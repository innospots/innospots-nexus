package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 权限设置页的插件功能树节点。
 *
 * <p>根为 {@link CatalogResourceType#MODULE}；其 {@link #children()} 为同模块下
 * {@link CatalogResourceType#PAGE} 子页面（PageDsl 页面键），用于菜单与页面级权限配置。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param resourceId    资源主键
 * @param ownerPluginId 来源插件
 * @param domainKey     领域键（通常由 {@link #routePath()} 解析）
 * @param moduleKey     模块 key
 * @param resourceType  资源类型
 * @param resourceKey   稳定资源 key
 * @param pageKey       PageDsl 页面键
 * @param routePath     页面路由 {@code /{domainKey}/{moduleKey}/{pageKey}}
 * @param displayName   展示名称
 * @param sortOrder     同级排序
 * @param children      子节点
 */
@Schema(name = "CatalogNodeVo", description = "目录树节点")
public record CatalogNodeVo(
        @Schema(description = "资源主键", required = true)
        String resourceId,
        @Schema(description = "来源插件", required = true)
        String ownerPluginId,
        @Schema(description = "领域键")
        String domainKey,
        @Schema(description = "模块 key", required = true)
        String moduleKey,
        @Schema(description = "资源类型", required = true)
        CatalogResourceType resourceType,
        @Schema(description = "稳定资源 key", required = true)
        String resourceKey,
        @Schema(description = "PageDsl 页面键")
        String pageKey,
        @Schema(description = "页面路由")
        String routePath,
        @Schema(description = "展示名称", required = true)
        String displayName,
        @Schema(description = "同级排序")
        Integer sortOrder,
        @Schema(description = "子节点", required = true)
        List<CatalogNodeVo> children
) {

    public CatalogNodeVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
