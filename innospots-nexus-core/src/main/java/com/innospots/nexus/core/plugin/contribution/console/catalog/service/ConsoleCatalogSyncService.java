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
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;
import com.innospots.nexus.base.ui.spec.loader.UiSpecLoader;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleModuleDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.MenuDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.model.CatalogSyncResult;

/**
 * 将已激活 Console Contribution 和 UiSpec 同步为宿主级目录索引。
 *
 * <p>Console Contribution 和 UiSpec 是唯一事实源；同步不会自动授权。</p>
 */
public final class ConsoleCatalogSyncService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final ConsoleContributionCatalog contributionCatalog;
    private final UiSpecLoader uiSpecLoader;

    /** 创建 Console 目录同步服务。 */
    public ConsoleCatalogSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            UiSpecLoader uiSpecLoader
    ) {
        this.resourceDao = require(resourceDao, "resourceDao");
        this.contributionCatalog = require(contributionCatalog, "contributionCatalog");
        this.uiSpecLoader = require(uiSpecLoader, "uiSpecLoader");
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
            UiSpec spec = uiSpecLoader.load(module.moduleKey(), page.pageKey());
            validatePageSpec(module, page, spec);
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
                    display(spec.pageInfo().title(), page.pageKey()),
                    order++));
            collectActions(definitions, ownerPluginId, module, spec, pageIdentity,
                    pageResourceKey);
            collectDatasources(definitions, ownerPluginId, module, spec, pageIdentity,
                    pageResourceKey);
            collectPages(definitions, ownerPluginId, module, page.children(), pageResourceKey);
        }
    }

    private void collectActions(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            UiSpec spec,
            String pageIdentity,
            String pageResourceKey
    ) {
        int order = 0;
        for (UiAction action : spec.actionDefinitions().values()) {
            collectAction(definitions, ownerPluginId, module, action, spec.datasources(),
                    pageIdentity, pageResourceKey, order++);
        }
    }

    private void collectAction(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            UiAction action,
            Map<String, UiDatasource> datasources,
            String pageIdentity,
            String pageResourceKey,
            int order
    ) {
        String actionId = requireText(action == null ? null : action.actionId(), "actionId");
        if (action.request() != null) {
            invalid("Inline action request is not supported: " + actionId);
        }
        if (action.datasourceKey() != null && !action.datasourceKey().isBlank()
                && !datasources.containsKey(action.datasourceKey())) {
            invalid("Action references an unknown datasource: " + actionId);
        }
        String resourceKey = "action:" + pageIdentity + "." + actionId;
        add(definitions, new ResourceDefinition(
                ownerPluginId,
                module.moduleKey(),
                CatalogResourceType.ACTION,
                resourceKey,
                pageResourceKey,
                pageIdentity,
                action.datasourceKey(),
                null,
                null,
                null,
                display(action.label(), actionId),
                order));
        int childOrder = 0;
        for (UiAction child : action.children()) {
            collectAction(definitions, ownerPluginId, module, child, datasources,
                    pageIdentity, pageResourceKey, childOrder++);
        }
    }

    private void collectDatasources(
            Map<String, ResourceDefinition> definitions,
            String ownerPluginId,
            ConsoleModuleDeclaration module,
            UiSpec spec,
            String pageIdentity,
            String pageResourceKey
    ) {
        Map<String, String> requestIdentities = new LinkedHashMap<>();
        int order = 0;
        for (Map.Entry<String, UiDatasource> entry : spec.datasources().entrySet()) {
            String datasourceKey = requireText(entry.getKey(), "datasourceKey");
            UiDatasource datasource = entry.getValue();
            if (datasource == null) {
                invalid("Datasource definition is required: " + datasourceKey);
            }
            String method = requireText(datasource.getMethod(), "datasource.method")
                    .toUpperCase();
            String url = normalizePath(requireText(datasource.getUrl(), "datasource.url"));
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
            UiSpec spec
    ) {
        if (spec == null || spec.pageInfo() == null
                || !page.pageKey().equals(spec.pageInfo().pageId())) {
            invalid("UiSpec pageInfo.pageId does not match "
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

    private static String display(Object value, String fallback) {
        if (value instanceof com.innospots.nexus.base.i18n.I18nObject i18n
                && i18n.defaultValue() != null && !i18n.defaultValue().isBlank()) {
            return i18n.defaultValue();
        }
        return fallback;
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
