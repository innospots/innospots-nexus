package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogDomainVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogPermissionGroupVo;
import com.innospots.nexus.console.catalog.domain.model.CatalogPermissionGroupDeclarations;
import com.innospots.nexus.console.catalog.domain.vo.CatalogViewItemVo;
import com.innospots.nexus.console.permission.domain.enums.PermissionGrantKeyType;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContribution;

/**
 * 从宿主级目录索引组装权限目录树。
 */
public final class ConsoleCatalogService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final CatalogPermissionCatalogAssembler grantAssembler;
    private final ConsoleContributionCatalog contributionCatalog;

    public ConsoleCatalogService(
            ConsoleCatalogResourceDao resourceDao,
            com.innospots.nexus.core.endpoint.grant.EndpointGrantManager endpointGrantManager
    ) {
        this(resourceDao, endpointGrantManager, null);
    }

    public ConsoleCatalogService(
            ConsoleCatalogResourceDao resourceDao,
            com.innospots.nexus.core.endpoint.grant.EndpointGrantManager endpointGrantManager,
            ConsoleContributionCatalog contributionCatalog
    ) {
        if (resourceDao == null) {
            throw ConsoleExceptions.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required");
        }
        this.resourceDao = resourceDao;
        this.grantAssembler = new CatalogPermissionCatalogAssembler(endpointGrantManager);
        this.contributionCatalog = contributionCatalog;
    }

    /**
     * @deprecated 使用 {@link #permissionCatalog()}，并读取各 {@link CatalogDomainVo#modules()}。
     */
    @Deprecated
    public List<CatalogNodeVo> tree() {
        return permissionCatalog().stream()
                .flatMap(domain -> domain.modules().stream())
                .toList();
    }

    public List<CatalogDomainVo> permissionCatalog() {
        List<ConsoleCatalogResourceEntity> resources = enabledResources();
        Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent = groupByParent(resources);
        List<ConsoleCatalogResourceEntity> moduleRoots = resources.stream()
                .filter(row -> CatalogResourceType.MODULE.name().equals(row.getResourceType()))
                .filter(row -> row.getParentResourceId() == null || row.getParentResourceId().isBlank())
                .sorted(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
        Map<String, DomainAccumulator> domains = new LinkedHashMap<>();
        for (ConsoleCatalogResourceEntity root : moduleRoots) {
            CatalogNodeVo moduleNode = toNode(root, childrenByParent);
            String groupKey = domainGroupKey(root.getOwnerPluginId(), root.getDomainKey());
            domains.computeIfAbsent(groupKey, ignored -> new DomainAccumulator(
                    root.getOwnerPluginId(),
                    root.getDomainKey()))
                    .modules()
                    .add(moduleNode);
        }
        List<CatalogDomainVo> result = new ArrayList<>();
        for (DomainAccumulator accumulator : domains.values()) {
            result.add(toDomainVo(accumulator));
        }
        result.sort(Comparator.comparing(domain -> domainSortKey(domain),
                Comparator.nullsLast(Integer::compareTo)));
        return List.copyOf(result);
    }

    /**
     * 返回当前启用目录中可持久化的全部授权项键。
     *
     * @return 页面访问项与操作项的稳定键
     */
    public Set<String> grantResourceKeys() {
        return grantKeyTypes().keySet();
    }

    /**
     * 返回授权项键及其 catalog 层级（权限目录 MODULE 组装项为 {@link PermissionGrantKeyType#MODULE}，
     * MENU 资源 {@code viewGroups} 声明项为 {@link PermissionGrantKeyType#MENU}）。
     *
     * @return 不可变映射
     */
    public Map<String, PermissionGrantKeyType> grantKeyTypes() {
        Map<String, PermissionGrantKeyType> types = new LinkedHashMap<>();
        for (CatalogDomainVo domain : permissionCatalog()) {
            for (CatalogNodeVo module : domain.modules()) {
                collectGrantKeyTypes(module, types);
            }
        }
        for (ConsoleCatalogResourceEntity row : enabledResources()) {
            if (!CatalogResourceType.MENU.name().equals(row.getResourceType())) {
                continue;
            }
            for (String key : CatalogPermissionGroupDeclarations.viewGrantKeys(row)) {
                if (key != null && !key.isBlank()) {
                    types.put(key, PermissionGrantKeyType.MENU);
                }
            }
        }
        return Map.copyOf(types);
    }

    private CatalogDomainVo toDomainVo(DomainAccumulator accumulator) {
        List<CatalogNodeVo> modules = List.copyOf(accumulator.modules());
        I18nObject displayName = resolveContributionDisplayName(
                accumulator.ownerPluginId(),
                accumulator.domainKey());
        String icon = resolveContributionIcon(accumulator.ownerPluginId(), accumulator.domainKey(), modules);
        return new CatalogDomainVo(
                accumulator.ownerPluginId(),
                accumulator.domainKey(),
                displayName,
                icon,
                modules);
    }

    private static Integer domainSortKey(CatalogDomainVo domain) {
        return domain.modules().stream()
                .map(CatalogNodeVo::orderIndex)
                .filter(index -> index != null)
                .min(Integer::compareTo)
                .orElse(null);
    }

    private I18nObject resolveContributionDisplayName(String ownerPluginId, String domainKey) {
        return resolveContribution(ownerPluginId, domainKey)
                .map(ConsolePluginContribution::displayName)
                .orElse(fallbackContributionLabel(domainKey));
    }

    private static String resolveContributionIcon(
            String ownerPluginId,
            String domainKey,
            List<CatalogNodeVo> modules
    ) {
        return modules.stream()
                .map(CatalogNodeVo::icon)
                .filter(icon -> icon != null && !icon.isBlank())
                .findFirst()
                .orElse(null);
    }

    private java.util.Optional<ConsolePluginContribution> resolveContribution(
            String ownerPluginId,
            String domainKey
    ) {
        if (contributionCatalog == null) {
            return java.util.Optional.empty();
        }
        return contributionCatalog.activeContributions().stream()
                .filter(item -> ownerPluginId.equals(item.ownerPluginId()))
                .map(ConsoleContributionCatalog.ActiveConsoleContribution::contribution)
                .filter(contribution -> domainKey.equals(contribution.domainKey()))
                .findFirst();
    }

    private static I18nObject fallbackContributionLabel(String domainKey) {
        return I18nObject.of(Map.of("en", domainKey));
    }

    private static String domainGroupKey(String ownerPluginId, String domainKey) {
        return ownerPluginId + "\u0000" + domainKey;
    }

    private CatalogNodeVo toNode(
            ConsoleCatalogResourceEntity row,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = childrenByParent.getOrDefault(
                row.getResourceId(), List.of());
        List<CatalogNodeVo> childNodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            childNodes.add(toNode(child, childrenByParent));
        }
        CatalogResourceType type = CatalogResourceType.valueOf(row.getResourceType());
        CatalogModuleGrantVo grants = null;
        if (type == CatalogResourceType.MODULE) {
            List<ConsoleCatalogResourceEntity> directMenus = children.stream()
                    .filter(child -> CatalogResourceType.MENU.name().equals(child.getResourceType()))
                    .toList();
            grants = grantAssembler.assembleModuleGrants(row, directMenus);
        }
        return new CatalogNodeVo(
                row.getResourceId(),
                row.getResourceKey(),
                type,
                row.getOwnerPluginId(),
                row.getDomainKey(),
                row.getModuleKey(),
                parseI18n(row.getDisplayName()),
                parseI18n(row.getDescription()),
                row.getSortOrder(),
                row.getEntryPageKey(),
                row.getIcon(),
                List.copyOf(childNodes),
                grants);
    }

    private static void collectGrantKeyTypes(
            CatalogNodeVo node,
            Map<String, PermissionGrantKeyType> types
    ) {
        CatalogModuleGrantVo grants = node.grants();
        if (grants != null) {
            for (CatalogPermissionGroupVo group : grants.viewGroups()) {
                putGrantKeyType(group.resourceKey(), PermissionGrantKeyType.MODULE, types);
            }
            for (CatalogPermissionGroupVo group : grants.operationGroups()) {
                putGrantKeyType(group.resourceKey(), PermissionGrantKeyType.MODULE, types);
                for (CatalogViewItemVo item : group.items()) {
                    addGrantKeyType(item, PermissionGrantKeyType.MODULE, types);
                }
            }
        }
        for (CatalogNodeVo child : node.children()) {
            collectGrantKeyTypes(child, types);
        }
    }

    private static void putGrantKeyType(
            String resourceKey,
            PermissionGrantKeyType type,
            Map<String, PermissionGrantKeyType> types
    ) {
        if (resourceKey != null && !resourceKey.isBlank()) {
            types.put(resourceKey, type);
        }
    }

    private static void addGrantKeyType(
            CatalogViewItemVo item,
            PermissionGrantKeyType type,
            Map<String, PermissionGrantKeyType> types
    ) {
        if (item != null && item.resourceKey() != null && !item.resourceKey().isBlank()) {
            types.put(item.resourceKey(), type);
        }
    }

    private List<ConsoleCatalogResourceEntity> enabledResources() {
        return resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name())
                        .orderByAsc(ConsoleCatalogResourceEntity::getSortOrder));
    }

    private static Map<String, List<ConsoleCatalogResourceEntity>> groupByParent(
            List<ConsoleCatalogResourceEntity> resources
    ) {
        Map<String, List<ConsoleCatalogResourceEntity>> map = new LinkedHashMap<>();
        for (ConsoleCatalogResourceEntity resource : resources) {
            String parentId = resource.getParentResourceId();
            if (parentId == null || parentId.isBlank()) {
                continue;
            }
            map.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(resource);
        }
        for (List<ConsoleCatalogResourceEntity> children : map.values()) {
            children.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                    Comparator.nullsLast(Integer::compareTo)));
        }
        return map;
    }

    private static I18nObject parseI18n(String json) {
        if (json == null || json.isBlank()) {
            return I18nObject.of(Map.of());
        }
        return Jsons.fromJson(json, I18nObject.class);
    }

    private static final class DomainAccumulator {

        private final String ownerPluginId;
        private final String domainKey;
        private final List<CatalogNodeVo> modules = new ArrayList<>();

        private DomainAccumulator(String ownerPluginId, String domainKey) {
            this.ownerPluginId = ownerPluginId;
            this.domainKey = domainKey;
        }

        private String ownerPluginId() {
            return ownerPluginId;
        }

        private String domainKey() {
            return domainKey;
        }

        private List<CatalogNodeVo> modules() {
            return modules;
        }
    }
}
