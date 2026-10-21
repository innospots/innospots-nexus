package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;

/**
 * 从宿主级目录索引组装权限目录树。
 */
public final class ConsoleCatalogService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final CatalogPermissionCatalogAssembler grantAssembler;

    public ConsoleCatalogService(
            ConsoleCatalogResourceDao resourceDao,
            ConsoleContributionCatalog contributionCatalog,
            com.innospots.nexus.console.endpoint.grant.EndpointGrantManager endpointGrantManager
    ) {
        if (resourceDao == null) {
            throw ConsoleExceptions.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required");
        }
        this.resourceDao = resourceDao;
        this.grantAssembler = new CatalogPermissionCatalogAssembler(
                contributionCatalog, endpointGrantManager);
    }

    /**
     * @deprecated 使用 {@link #permissionCatalog()}
     */
    @Deprecated
    public List<CatalogNodeVo> tree() {
        return permissionCatalog();
    }

    public List<CatalogNodeVo> permissionCatalog() {
        List<ConsoleCatalogResourceEntity> resources = enabledResources();
        Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent = groupByParent(resources);
        List<ConsoleCatalogResourceEntity> roots = resources.stream()
                .filter(row -> CatalogResourceType.MODULE.name().equals(row.getResourceType()))
                .filter(row -> row.getParentResourceId() == null || row.getParentResourceId().isBlank())
                .sorted(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .toList();
        List<CatalogNodeVo> nodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity root : roots) {
            nodes.add(toNode(root, childrenByParent));
        }
        return List.copyOf(nodes);
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
        com.innospots.nexus.console.catalog.domain.vo.CatalogModuleGrantVo grants = null;
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
}
