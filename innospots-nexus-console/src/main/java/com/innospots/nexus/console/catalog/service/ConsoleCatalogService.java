package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.core.plugin.contribution.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;

/** 从宿主级目录索引组装权限设置树。 */
public final class ConsoleCatalogService {

    private final ConsoleCatalogResourceDao resourceDao;

    /** 创建目录树查询服务。 */
    public ConsoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        if (resourceDao == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required");
        }
        this.resourceDao = resourceDao;
    }

    /**
     * 返回已启用的插件目录资源树。
     *
     * @return 根节点列表
     */
    public List<CatalogNodeVo> tree() {
        List<ConsoleCatalogResourceEntity> resources = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name())
                        .orderByAsc(ConsoleCatalogResourceEntity::getSortOrder));
        return buildTree(resources);
    }

    static List<CatalogNodeVo> buildTree(List<ConsoleCatalogResourceEntity> resources) {
        Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent = new LinkedHashMap<>();
        List<ConsoleCatalogResourceEntity> roots = new ArrayList<>();
        for (ConsoleCatalogResourceEntity resource : resources) {
            String parentId = resource.getParentResourceId();
            if (parentId == null || parentId.isBlank()) {
                roots.add(resource);
            } else {
                childrenByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(resource);
            }
        }
        roots.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<CatalogNodeVo> nodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity root : roots) {
            nodes.add(toNode(root, childrenByParent));
        }
        return List.copyOf(nodes);
    }

    private static CatalogNodeVo toNode(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = childrenByParent.getOrDefault(
                resource.getResourceId(), List.of());
        children = new ArrayList<>(children);
        children.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<CatalogNodeVo> childNodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            childNodes.add(toNode(child, childrenByParent));
        }
        return new CatalogNodeVo(
                resource.getResourceId(),
                resource.getOwnerPluginId(),
                resource.getModuleKey(),
                CatalogResourceType.valueOf(resource.getResourceType()),
                resource.getResourceKey(),
                resource.getPageKey(),
                resource.getRoutePath(),
                resource.getDisplayName(),
                resource.getSortOrder(),
                childNodes);
    }
}
