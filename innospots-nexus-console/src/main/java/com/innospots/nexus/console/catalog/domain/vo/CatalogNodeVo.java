package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;

/**
 * 权限设置页的插件功能树节点。
 *
 * @param resourceId    资源主键
 * @param ownerPluginId 来源插件
 * @param moduleKey     模块 key
 * @param resourceType  资源类型
 * @param resourceKey   稳定资源 key
 * @param pageKey       页面标识
 * @param routePath     页面路由
 * @param displayName   展示名称
 * @param sortOrder     同级排序
 * @param children      子节点
 */
public record CatalogNodeVo(
        String resourceId,
        String ownerPluginId,
        String moduleKey,
        CatalogResourceType resourceType,
        String resourceKey,
        String pageKey,
        String routePath,
        String displayName,
        Integer sortOrder,
        List<CatalogNodeVo> children
) {

    public CatalogNodeVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
