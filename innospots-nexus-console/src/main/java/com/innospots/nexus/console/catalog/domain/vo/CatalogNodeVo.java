package com.innospots.nexus.console.catalog.domain.vo;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 权限目录树节点（MODULE / MENU）。
 */
@Schema(name = "CatalogNodeVo", description = "权限目录树节点")
public record CatalogNodeVo(
        @Schema(description = "资源主键")
        String resourceId,
        @Schema(description = "稳定资源 key", required = true)
        String resourceKey,
        @Schema(description = "资源类型", required = true)
        CatalogResourceType resourceType,
        @Schema(description = "来源插件", required = true)
        String ownerPluginId,
        @Schema(description = "领域键")
        String domainKey,
        @Schema(description = "模块 key", required = true)
        String moduleKey,
        @Schema(description = "展示名称", required = true)
        I18nObject displayName,
        @Schema(description = "描述")
        I18nObject description,
        @Schema(description = "同级排序")
        Integer orderIndex,
        @Schema(description = "叶子 MODULE 或 MENU 入口页键")
        String entryPageKey,
        @Schema(description = "入口图标")
        String icon,
        @Schema(description = "子节点", required = true)
        List<CatalogNodeVo> children,
        @Schema(description = "MODULE 授权面")
        CatalogModuleGrantVo grants
) {

    public CatalogNodeVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
