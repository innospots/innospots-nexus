package com.innospots.nexus.console.catalog.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeOperationVo;
import com.innospots.nexus.console.catalog.domain.vo.CatalogNodeVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantDescriptorVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.EndpointModuleGrantCatalogVo;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 从宿主级目录索引组装权限设置树。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ConsoleCatalogService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final EndpointGrantManager endpointGrantManager;

    /**
     * 创建目录树查询服务。
     */
    public ConsoleCatalogService(
            ConsoleCatalogResourceDao resourceDao,
            EndpointGrantManager endpointGrantManager
    ) {
        if (resourceDao == null) {
            throw ConsoleExceptions.build(NexusStatusCode.CONFIG_ERROR, "resourceDao is required");
        }
        if (endpointGrantManager == null) {
            throw ConsoleExceptions.build(NexusStatusCode.CONFIG_ERROR, "endpointGrantManager is required");
        }
        this.resourceDao = resourceDao;
        this.endpointGrantManager = endpointGrantManager;
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
        Map<String, List<CatalogNodeOperationVo>> operationsByModule = loadOperationsByModule();
        return buildTree(resources, operationsByModule);
    }

    private Map<String, List<CatalogNodeOperationVo>> loadOperationsByModule() {
        Map<String, List<CatalogNodeOperationVo>> operationsByModule = new LinkedHashMap<>();
        for (Map.Entry<String, EndpointModuleGrantCatalogVo> entry
                : endpointGrantManager.allModuleCatalogs().entrySet()) {
            List<CatalogNodeOperationVo> operations = new ArrayList<>();
            EndpointModuleGrantCatalogVo catalog = entry.getValue();
            for (EndpointGrantDescriptorVo descriptor : catalog.operable()) {
                operations.add(toOperation(descriptor));
            }
            operations.sort(Comparator.comparing(CatalogNodeOperationVo::name));
            operationsByModule.put(entry.getKey(), List.copyOf(operations));
        }
        return operationsByModule;
    }

    private static CatalogNodeOperationVo toOperation(EndpointGrantDescriptorVo descriptor) {
        return new CatalogNodeOperationVo(
                descriptor.name(),
                descriptor.label(),
                descriptor.action(),
                descriptor.path());
    }

    static List<CatalogNodeVo> buildTree(
            List<ConsoleCatalogResourceEntity> resources,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
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
            nodes.add(toPermissionTreeNode(root, childrenByParent, operationsByModule));
        }
        return List.copyOf(nodes);
    }

    private static CatalogNodeVo toPermissionTreeNode(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
        if (resource.getResourceType().equals(CatalogResourceType.MODULE.name())) {
            return moduleNode(resource, childrenByParent, operationsByModule);
        }
        return toNode(resource, childrenByParent, operationsByModule);
    }

    private static CatalogNodeVo moduleNode(
            ConsoleCatalogResourceEntity module,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(module, childrenByParent);
        List<CatalogNodeVo> pageNodes = new ArrayList<>();
        String domainKey = null;
        for (ConsoleCatalogResourceEntity child : children) {
            if (!CatalogResourceType.PAGE.name().equals(child.getResourceType())) {
                continue;
            }
            CatalogNodeVo pageNode = buildPagePermissionNode(child, childrenByParent, operationsByModule);
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
                operationsFor(module.getModuleKey(), operationsByModule),
                List.copyOf(pageNodes));
    }

    private static CatalogNodeVo buildPagePermissionNode(
            ConsoleCatalogResourceEntity page,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(page, childrenByParent);
        List<CatalogNodeVo> childPages = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            if (CatalogResourceType.PAGE.name().equals(child.getResourceType())) {
                childPages.add(buildPagePermissionNode(child, childrenByParent, operationsByModule));
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
                operationsFor(page.getModuleKey(), operationsByModule),
                List.copyOf(childPages));
    }

    private static CatalogNodeVo toNode(
            ConsoleCatalogResourceEntity resource,
            Map<String, List<ConsoleCatalogResourceEntity>> childrenByParent,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
        List<ConsoleCatalogResourceEntity> children = sortedChildren(resource, childrenByParent);
        List<CatalogNodeVo> childNodes = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            childNodes.add(toNode(child, childrenByParent, operationsByModule));
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
                operationsFor(resource.getModuleKey(), operationsByModule),
                childNodes);
    }

    private static List<CatalogNodeOperationVo> operationsFor(
            String moduleKey,
            Map<String, List<CatalogNodeOperationVo>> operationsByModule
    ) {
        if (moduleKey == null || moduleKey.isBlank()) {
            return List.of();
        }
        return operationsByModule.getOrDefault(moduleKey, List.of());
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
        return ConsoleModuleDescriptor.domainKeyFromPageRoute(routePath);
    }
}
