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
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 从宿主级目录索引组装权限设置树。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ConsoleCatalogService {

    private final ConsoleCatalogResourceDao resourceDao;

    /**
     * 创建目录树查询服务。
     */
    public ConsoleCatalogService(ConsoleCatalogResourceDao resourceDao) {
        if (resourceDao == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required");
        }
        this.resourceDao = resourceDao;
    }

    /**
     * 返回已启用的插件目录资源树（模块下 children 为 PageDsl 子页面）。
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
            nodes.add(toPermissionTreeNode(root, childrenByParent));
        }
        return List.copyOf(nodes);
    }

    private static CatalogNodeVo toPermissionTreeNode(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        if (resource.getResourceType().equals(CatalogResourceType.MODULE.name())) {
            return moduleNode(resource, childrenByParent);
        }
        return toNode(resource, childrenByParent);
    }

    private static CatalogNodeVo moduleNode(
            ConsoleCatalogResourceEntity module,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(module, childrenByParent);
        List<CatalogNodeVo> pageNodes = new ArrayList<>();
        String domainKey = null;
        for (ConsoleCatalogResourceEntity child : children) {
            if (!CatalogResourceType.PAGE.name().equals(child.getResourceType())) {
                continue;
            }
            CatalogNodeVo pageNode = buildPagePermissionNode(child, childrenByParent);
            pageNodes.add(pageNode);
            if (domainKey == null) {
                domainKey = pageNode.domainKey();
            }
        }
        return new CatalogNodeVo(
                module.getResourceId(),
                module.getOwnerPluginId(),
                domainKey,
                module.getModuleKey(),
                CatalogResourceType.MODULE,
                module.getResourceKey(),
                null,
                null,
                module.getDisplayName(),
                module.getSortOrder(),
                List.copyOf(pageNodes));
    }

    private static CatalogNodeVo buildPagePermissionNode(
            ConsoleCatalogResourceEntity page,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(page, childrenByParent);
        List<CatalogNodeVo> childPages = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            if (CatalogResourceType.PAGE.name().equals(child.getResourceType())) {
                childPages.add(buildPagePermissionNode(child, childrenByParent));
            }
        }
        return new CatalogNodeVo(
                page.getResourceId(),
                page.getOwnerPluginId(),
                domainKeyFromRoute(page.getRoutePath()),
                page.getModuleKey(),
                CatalogResourceType.PAGE,
                page.getResourceKey(),
                page.getPageKey(),
                page.getRoutePath(),
                page.getDisplayName(),
                page.getSortOrder(),
                List.copyOf(childPages));
    }

    private static CatalogNodeVo toNode(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(resource, childrenByParent);
        List<CatalogNodeVo> childNodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            childNodes.add(toNode(child, childrenByParent));
        }
        return new CatalogNodeVo(
                resource.getResourceId(),
                resource.getOwnerPluginId(),
                domainKeyFromRoute(resource.getRoutePath()),
                resource.getModuleKey(),
                CatalogResourceType.valueOf(resource.getResourceType()),
                resource.getResourceKey(),
                resource.getPageKey(),
                resource.getRoutePath(),
                resource.getDisplayName(),
                resource.getSortOrder(),
                childNodes);
    }

    private static List<ConsoleCatalogResourceEntity> sortedChildren(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent
    ) {
        List<ConsoleCatalogResourceEntity> children = new ArrayList<>(
                childrenByParent.getOrDefault(resource.getResourceId(), List.of()));
        children.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        return children;
    }

    static String domainKeyFromRoute(String routePath) {
        if (routePath == null || routePath.isBlank()) {
            return null;
        }
        String normalized = routePath.startsWith("/") ? routePath.substring(1) : routePath;
        int slash = normalized.indexOf('/');
        if (slash < 0) {
            return normalized.isBlank() ? null : normalized;
        }
        String domainKey = normalized.substring(0, slash);
        return domainKey.isBlank() ? null : domainKey;
    }
}
