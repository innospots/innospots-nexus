package com.innospots.nexus.console.permission.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 面向管理端和前端的权限资源目录视图。
 */
@Schema(name = "PermissionResourceVo", description = "权限资源视图")
public record PermissionResourceVo(
        @Schema(description = "资源记录主键", required = true)
        String resourceId,
        @Schema(description = "来源插件稳定身份", required = true)
        String ownerPluginId,
        @Schema(description = "领域键")
        String domainKey,
        @Schema(description = "所属模块 key", required = true)
        String moduleKey,
        @Schema(description = "资源类型", required = true)
        CatalogResourceType resourceType,
        @Schema(description = "稳定资源 key", required = true)
        String resourceKey,
        @Schema(description = "资源父节点主键")
        String parentResourceId,
        @Schema(description = "MENU 入口页 PageDsl 键")
        String entryPageKey,
        @Schema(description = "目录展示名称", required = true)
        String displayName,
        @Schema(description = "同级排序值")
        Integer sortOrder,
        @Schema(description = "资源状态", required = true)
        String status
) {

    public static PermissionResourceVo from(ConsoleCatalogResourceEntity entity) {
        return new PermissionResourceVo(
                entity.getResourceId(),
                entity.getOwnerPluginId(),
                entity.getDomainKey(),
                entity.getModuleKey(),
                CatalogResourceType.valueOf(entity.getResourceType()),
                entity.getResourceKey(),
                entity.getParentResourceId(),
                entity.getEntryPageKey(),
                entity.getDisplayName(),
                entity.getSortOrder(),
                entity.getStatus());
    }
}
