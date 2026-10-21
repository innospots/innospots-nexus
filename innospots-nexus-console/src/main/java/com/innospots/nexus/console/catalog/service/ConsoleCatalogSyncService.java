package com.innospots.nexus.console.catalog.service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.model.CatalogSyncResult;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePermissionGroupDeclaration;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

/**
 * 将已激活 console@2 贡献同步为宿主级目录索引（仅 MODULE / MENU）。
 */
public final class ConsoleCatalogSyncService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final ConsoleContributionCatalog contributionCatalog;

    public ConsoleCatalogSyncService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog
    ) {
        this.resourceDao = require(resourceDao, "resourceDao");
        this.contributionCatalog = require(contributionCatalog, "contributionCatalog");
    }

    public CatalogSyncResult sync() {
        List<ResourceDefinition> definitions = discover();
        Map<String, ConsoleCatalogResourceEntity> existing = loadExisting();
        int created = 0;
        int updated = 0;
        Set<String> activeKeys = new HashSet<>();

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
            ConsolePluginContribution contribution = active.contribution();
            String domainKey = contribution.domainKey();
            for (ConsoleContributeNode module : contribution.modules()) {
                walkModule(ownerPluginId, domainKey, module, null, definitions, new LinkedHashMap<>());
            }
        }
        return List.copyOf(definitions.values());
    }

    private void walkModule(
            String ownerPluginId,
            String domainKey,
            ConsoleContributeNode module,
            String parentResourceKey,
            Map<String, ResourceDefinition> definitions,
            Map<String, String> resourceKeyByPath
    ) {
        String moduleResourceKey = module.moduleResourceKey();
        boolean leafModule = module.children().isEmpty();
        String groupsJson = leafModule ? groupsJson(module.viewGroups()) : null;
        String operationGroupsJson = leafModule ? groupsJson(module.operationGroups()) : null;
        add(definitions, new ResourceDefinition(
                ownerPluginId,
                domainKey,
                module.moduleKey(),
                CatalogResourceType.MODULE,
                moduleResourceKey,
                parentResourceKey,
                module.entryPageKey(),
                module.icon(),
                groupsJson,
                operationGroupsJson,
                i18nJson(module.displayName()),
                i18nJson(module.description()),
                module.orderIndex()));
        resourceKeyByPath.put(module.moduleKey(), moduleResourceKey);

        for (ConsoleContributeNode child : module.children()) {
            if (child.isMenu()) {
                add(definitions, new ResourceDefinition(
                        ownerPluginId,
                        domainKey,
                        module.moduleKey(),
                        CatalogResourceType.MENU,
                        child.menuResourceKey(module.moduleKey()),
                        moduleResourceKey,
                        child.entryPageKey(),
                        child.icon(),
                        groupsJson(module.viewGroups()),
                        groupsJson(module.operationGroups()),
                        i18nJson(child.displayName()),
                        i18nJson(child.description()),
                child.orderIndex()));
            } else if (child.isModule()) {
                walkModule(ownerPluginId, domainKey, child, moduleResourceKey, definitions, resourceKeyByPath);
            } else {
                invalid("illegal child under module " + module.moduleKey());
            }
        }
    }

    private static String groupsJson(List<ConsolePermissionGroupDeclaration> groups) {
        return Jsons.toJson(groups);
    }

    private static String i18nJson(I18nObject value) {
        if (value == null) {
            return null;
        }
        return Jsons.toJson(value);
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
        entity.setDomainKey(definition.domainKey());
        entity.setModuleKey(definition.moduleKey());
        entity.setResourceType(definition.type().name());
        entity.setResourceKey(definition.resourceKey());
        entity.setParentResourceId(parentId);
        entity.setEntryPageKey(definition.entryPageKey());
        entity.setDisplayName(definition.displayName());
        entity.setDescription(definition.description());
        entity.setSortOrder(definition.sortOrder());
        entity.setIcon(definition.icon());
        entity.setViewGroups(definition.viewGroups());
        entity.setOperationGroups(definition.operationGroups());
        entity.setStatus(BasicStatus.ENABLED.name());
        entity.setSecurityRealm(securityRealmForPlugin(definition.ownerPluginId()));
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
                || !Objects.equals(entity.getDomainKey(), definition.domainKey())
                || !Objects.equals(entity.getModuleKey(), definition.moduleKey())
                || !Objects.equals(entity.getResourceType(), definition.type().name())
                || !Objects.equals(entity.getParentResourceId(), parentId)
                || !Objects.equals(entity.getEntryPageKey(), definition.entryPageKey())
                || !Objects.equals(entity.getDisplayName(), definition.displayName())
                || !Objects.equals(entity.getDescription(), definition.description())
                || !Objects.equals(entity.getSortOrder(), definition.sortOrder())
                || !Objects.equals(entity.getIcon(), definition.icon())
                || !Objects.equals(entity.getViewGroups(), definition.viewGroups())
                || !Objects.equals(entity.getOperationGroups(), definition.operationGroups())
                || !BasicStatus.ENABLED.name().equals(entity.getStatus());
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

    private static String securityRealmForPlugin(String ownerPluginId) {
        if ("com.innospots.nexus.platform.console".equals(ownerPluginId)) {
            return SecurityRealm.PLATFORM.name();
        }
        return currentSecurityRealm();
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
        throw ConsoleExceptions.build(NexusStatusCode.CONFIG_ERROR.fullCode(), message);
    }

    private record ResourceDefinition(
            String ownerPluginId,
            String domainKey,
            String moduleKey,
            CatalogResourceType type,
            String resourceKey,
            String parentResourceKey,
            String entryPageKey,
            String icon,
            String viewGroups,
            String operationGroups,
            String displayName,
            String description,
            int sortOrder
    ) {
    }
}
