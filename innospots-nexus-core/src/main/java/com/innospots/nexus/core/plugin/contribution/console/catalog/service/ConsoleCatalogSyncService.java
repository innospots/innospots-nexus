package com.innospots.nexus.core.plugin.contribution.console.catalog.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.datasource.DataSourceConfig;
import com.innospots.nexus.base.ui.spec.datasource.HttpDataSource;
import com.innospots.nexus.base.ui.spec.datasource.ServiceDataSource;
import com.innospots.nexus.base.ui.spec.loader.PageDslLoader;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.model.CatalogSyncResult;

/**
 * 将已激活 Console Contribution 和 PageDsl 同步为宿主级目录索引。
 *
 * <p>Console Contribution 和 PageDsl 是唯一事实源；同步不会自动授权。</p>
 */
public final class ConsoleCatalogSyncService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final ConsoleContributionCatalog contributionCatalog;
    private final PageDslLoader pageDslLoader;

    /** 创建 Console 目录同步服务。 */
    public ConsoleCatalogSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            PageDslLoader pageDslLoader
    ) {
        this.resourceDao = require(resourceDao, "resourceDao");
        this.contributionCatalog = require(contributionCatalog, "contributionCatalog");
        this.pageDslLoader = require(pageDslLoader, "pageDslLoader");
    }

    /**
     * 同步当前宿主全部 ACTIVE 插件贡献的模块、菜单、页面、action 和 datasource。
     *
     * @return 本次创建、更新和禁用的资源数量
     */
    public CatalogSyncResult sync() {
        List<ResourceDefinition> definitions = discover();
        Map<String, ConsoleCatalogResourceEntity> existing = loadExisting();
        int created = 0;
        int updated = 0;
        Set<String> activeKeys = new HashSet<>();

        // 先发现完整资源集合，确保任意定义校验失败时不会留下半套目录。
        for (ResourceDefinition definition : definitions) {
            activeKeys.add(definition.resourceKey());
            ConsoleCatalogResourceEntity entity = existing.get(definition.resourceKey());
            boolean isNew = entity == null;
            if (isNew) {
                entity = new ConsoleCatalogResourceEntity();
            }
            if (isNew) {
                apply(entity, definition, existing);
                resourceDao.insert(entity);
                existing.put(definition.resourceKey(), entity);
                created++;
            } else if (changed(entity, definition, existing)) {
                apply(entity, definition, existing);
                resourceDao.updateById(entity);
                updated++;
            }
        }

        int disabled = disableMissing(existing, activeKeys);
        return new CatalogSyncResult(created, updated, disabled);
    }

    private List<ResourceDefinition> discover() {
        Map<String, ResourceDefinition> definitions = new LinkedHashMap<>();
        for (ConsoleContributionCatalog.ActiveConsoleContribution active
                : contributionCatalog.activeContributions()) {
            String ownerPluginId = active.ownerPluginId();
            for (ConsoleModuleDeclaration module : active.contribution().modules()) {
                add(definitions, moduleDefinition(ownerPluginId, module));
                collectMenus(definitions, ownerPluginId, module, module.menuTree(), null);
                collectPages(definitions, ownerPluginId, module, module.pages(), null);
            }
        }
        return List.copyOf(definitions.values());
    }

    private void collectMenus(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            List<MenuDeclaration> menus,
            String parentResourceKey
    ) {
        int order = 0;
        for (MenuDeclaration menu : menus) {
            String resourceKey = menu.resourceKey(module.moduleKey());
            String pageKey = menu.pageKey() == null
                    ? null
                    : pageIdentity(module.moduleKey(), menu.pageKey());
            add(definitions, new ResourceDefinition(
                    ownerPluginId,
                    module.moduleKey(),
                    CatalogResourceType.MENU,
                    resourceKey,
                    parentResourceKey == null ? module.resourceKey() : parentResourceKey,
                    pageKey,
                    null,
                    null,
                    null,
                    null,
                    display(menu.title(), menu.menuKey()),
                    order++));
            collectMenus(definitions, ownerPluginId, module, menu.children(), resourceKey);
        }
    }

    private void collectPages(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            List<UiSpecPageDeclaration> pages,
            String parentResourceKey
    ) {
        int order = 0;
        for (UiSpecPageDeclaration page : pages) {
            String pageIdentity = pageIdentity(module.moduleKey(), page.pageKey());
            String pageResourceKey = page.resourceKey(module.moduleKey());
            PageDsl document = pageDslLoader.load(module.moduleKey(), page.pageKey());
            validatePageSpec(module, page, document);
            add(definitions, new ResourceDefinition(
                    ownerPluginId,
                    module.moduleKey(),
                    CatalogResourceType.PAGE,
                    pageResourceKey,
                    parentResourceKey == null ? module.resourceKey() : parentResourceKey,
                    pageIdentity,
                    null,
                    page.pagePath(),
                    null,
                    null,
                    display(document.getPage() == null ? null : document.getPage().getTitle(), page.pageKey()),
                    order++));
            collectActions(definitions, ownerPluginId, module, document, pageIdentity,
                    pageResourceKey);
            collectDatasources(definitions, ownerPluginId, module, document, pageIdentity,
                    pageResourceKey);
            collectPages(definitions, ownerPluginId, module, page.children(), pageResourceKey);
        }
    }

    private void collectActions(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            PageDsl document,
            String pageIdentity,
            String pageResourceKey
    ) {
        int order = 0;
        for (Map.Entry<String, ActionOrList> entry : document.actions().entrySet()) {
            String actionName = requireText(entry.getKey(), "actionName");
            for (ActionConfig action : entry.getValue().actions()) {
                collectAction(definitions, ownerPluginId, module, actionName, action,
                        document.dataSources(), pageIdentity, pageResourceKey, order++);
            }
        }
    }

    private void collectAction(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            String actionName,
            ActionConfig action,
            Map<String, DataSourceConfig> dataSources,
            String pageIdentity,
            String pageResourceKey,
            int order
    ) {
        requireText(actionName, "actionName");
        if (action == null || !hasText(action.getAction())) {
            invalid("Action registry name is required: " + actionName);
        }
        String datasourceKey = resolveDatasourceKey(action);
        if (datasourceKey != null && !dataSources.containsKey(datasourceKey)) {
            invalid("Action references an unknown datasource: " + actionName);
        }
        String resourceKey = "action:" + pageIdentity + "." + actionName;
        add(definitions, new ResourceDefinition(
                ownerPluginId,
                module.moduleKey(),
                CatalogResourceType.ACTION,
                resourceKey,
                pageResourceKey,
                pageIdentity,
                datasourceKey,
                null,
                null,
                null,
                display(action.getId(), actionName),
                order));
    }

    private void collectDatasources(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            PageDsl document,
            String pageIdentity,
            String pageResourceKey
    ) {
        Map<String, String> requestIdentities = new LinkedHashMap<>();
        int order = 0;
        for (Map.Entry<String, DataSourceConfig> entry : document.dataSources().entrySet()) {
            String datasourceKey = requireText(entry.getKey(), "datasourceKey");
            DataSourceConfig dataSource = entry.getValue();
            if (dataSource == null) {
                invalid("Datasource definition is required: " + datasourceKey);
            }
            String method;
            String url;
            if (dataSource instanceof HttpDataSource httpDataSource) {
                if (httpDataSource.getRequest() == null) {
                    invalid("HTTP datasource request is required: " + datasourceKey);
                }
                method = requireText(httpDataSource.getRequest().getMethod(), "datasource.method")
                        .toUpperCase();
                url = normalizePath(requireText(httpDataSource.getRequest().getUrl(), "datasource.url"));
            } else if (dataSource instanceof ServiceDataSource serviceDataSource) {
                method = "SERVICE";
                url = "/" + requireText(serviceDataSource.getService(), "datasource.service");
            } else {
                continue;
            }
            String requestIdentity = method + " " + url;
            if (requestIdentities.put(requestIdentity, datasourceKey) != null) {
                invalid("Multiple datasources match " + requestIdentity + " on " + pageIdentity);
            }
            add(definitions, new ResourceDefinition(
                    ownerPluginId,
                    module.moduleKey(),
                    CatalogResourceType.DATASOURCE,
                    "datasource:" + pageIdentity + "." + datasourceKey,
                    pageResourceKey,
                    pageIdentity,
                    datasourceKey,
                    null,
                    method,
                    url,
                    datasourceKey,
                    order++));
        }
    }

    private ResourceDefinition moduleDefinition(
            String ownerPluginId,
            ConsoleModuleDeclaration module
    ) {
        return new ResourceDefinition(
                ownerPluginId,
                module.moduleKey(),
                CatalogResourceType.MODULE,
                module.resourceKey(),
                null,
                null,
                null,
                null,
                null,
                null,
                display(module.displayName(), module.moduleKey()),
                0);
    }

    private void validatePageSpec(
            ConsoleModuleDeclaration module,
            UiSpecPageDeclaration page,
            PageDsl document
    ) {
        if (document == null || document.getPage() == null
                || !page.pageKey().equals(document.getPage().getId())) {
            invalid("PageDsl page.id does not match "
                    + module.moduleKey() + "." + page.pageKey());
        }
    }

    private void add(Map<String, ResourceDefinition> definitions, ResourceDefinition definition) {
        ResourceDefinition previous = definitions.putIfAbsent(definition.resourceKey(), definition);
        if (previous != null && !previous.equals(definition)) {
            invalid("Conflicting catalog resource: " + definition.resourceKey());
        }
    }

    private Map<String, ConsoleCatalogResourceEntity> loadExisting() {
        Map<String, ConsoleCatalogResourceEntity> result = new LinkedHashMap<>();
        for (ConsoleCatalogResourceEntity entity : resourceDao.selectList(null)) {
            result.put(entity.getResourceKey(), entity);
        }
        return result;
    }

    private void apply(
            ConsoleCatalogResourceEntity entity,
            ResourceDefinition definition,
            Map<String, ConsoleCatalogResourceEntity> resources
    ) {
        String parentId = definition.parentResourceKey() == null
                ? null
                : resourceId(resources, definition.parentResourceKey());
        entity.setOwnerPluginId(definition.ownerPluginId());
        entity.setModuleKey(definition.moduleKey());
        entity.setResourceType(definition.type().name());
        entity.setResourceKey(definition.resourceKey());
        entity.setParentResourceId(parentId);
        entity.setPageKey(definition.pageKey());
        entity.setDatasourceKey(definition.datasourceKey());
        entity.setRoutePath(definition.routePath());
        entity.setRequestMethod(definition.requestMethod());
        entity.setRequestUrl(definition.requestUrl());
        entity.setDisplayName(definition.displayName());
        entity.setSortOrder(definition.sortOrder());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setSecurityRealm(currentSecurityRealm());
    }

    private boolean changed(
            ConsoleCatalogResourceEntity entity,
            ResourceDefinition definition,
            Map<String, ConsoleCatalogResourceEntity> resources
    ) {
        String parentId = definition.parentResourceKey() == null
                ? null
                : resourceId(resources, definition.parentResourceKey());
        return !Objects.equals(entity.getOwnerPluginId(), definition.ownerPluginId())
                || !Objects.equals(entity.getModuleKey(), definition.moduleKey())
                || !Objects.equals(entity.getResourceType(), definition.type().name())
                || !Objects.equals(entity.getParentResourceId(), parentId)
                || !Objects.equals(entity.getPageKey(), definition.pageKey())
                || !Objects.equals(entity.getDatasourceKey(), definition.datasourceKey())
                || !Objects.equals(entity.getRoutePath(), definition.routePath())
                || !Objects.equals(entity.getRequestMethod(), definition.requestMethod())
                || !Objects.equals(entity.getRequestUrl(), definition.requestUrl())
                || !Objects.equals(entity.getDisplayName(), definition.displayName())
                || !Objects.equals(entity.getSortOrder(), definition.sortOrder())
                || !BasicStatus.ENABLED.name().equals(entity.getStatus())
                || !Objects.equals(entity.getSecurityRealm(), currentSecurityRealm());
    }

    private int disableMissing(
            Map<String, ConsoleCatalogResourceEntity> existing,
            Set<String> activeKeys
    ) {
        int disabled = 0;
        for (ConsoleCatalogResourceEntity entity : existing.values()) {
            if (!activeKeys.contains(entity.getResourceKey())
                    && BasicStatus.ENABLED.name().equals(entity.getStatus())) {
                entity.setStatus(BasicStatus.DISABLED.name());
                resourceDao.updateById(entity);
                disabled++;
            }
        }
        return disabled;
    }

    private String resourceId(
            Map<String, ConsoleCatalogResourceEntity> resources,
            String resourceKey
    ) {
        ConsoleCatalogResourceEntity parent = resources.get(resourceKey);
        if (parent == null || parent.getResourceId() == null) {
            invalid("Missing parent catalog resource: " + resourceKey);
        }
        return parent.getResourceId();
    }

    private static String pageIdentity(String moduleKey, String pageKey) {
        return moduleKey + "." + pageKey;
    }

    private static String normalizePath(String value) {
        int query = value.indexOf('?');
        int fragment = value.indexOf('#');
        int end = query < 0 ? value.length() : query;
        if (fragment >= 0) {
            end = Math.min(end, fragment);
        }
        String path = value.substring(0, end).trim();
        if (!path.startsWith("/")) {
            invalid("Datasource URL must be a path: " + value);
        }
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }

    private static String display(com.innospots.nexus.base.i18n.I18nObject value, String fallback) {
        if (value != null && value.defaultValue() != null && !value.defaultValue().isBlank()) {
            return value.defaultValue();
        }
        return fallback;
    }

    private static String display(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value;
        }
        return fallback;
    }

    private static String resolveDatasourceKey(ActionConfig action) {
        if (action.getParams() == null) {
            return null;
        }
        Object dataSource = action.getParams().get("dataSource");
        if (dataSource == null) {
            return null;
        }
        String key = String.valueOf(dataSource);
        if (key.isBlank()) {
            return null;
        }
        return key;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            invalid(field + " is required");
        }
        return value.trim();
    }

    private static String currentSecurityRealm() {
        String realm = TLC.securityRealm();
        if (realm == null || realm.isBlank()) {
            return "TENANT";
        }
        return realm;
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            invalid(field + " is required");
        }
        return value;
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }

    private record ResourceDefinition(
            String ownerPluginId,
            String moduleKey,
            CatalogResourceType type,
            String resourceKey,
            String parentResourceKey,
            String pageKey,
            String datasourceKey,
            String routePath,
            String requestMethod,
            String requestUrl,
            String displayName,
            int sortOrder
    ) {
    }

}
