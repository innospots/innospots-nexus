package com.innospots.nexus.console.permission.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;

/**
 * 面向管理端和前端的权限资源目录视图。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(name = "PermissionResourceVo", description = "权限资源视图")
public record PermissionResourceVo(
        @Schema(description = "资源记录主键", required = true)
        String resourceId,
        @Schema(description = "来源插件稳定身份", required = true)
        String ownerPluginId,
        @Schema(description = "所属模块 key", required = true)
        String moduleKey,
        @Schema(description = "资源类型", required = true)
        CatalogResourceType resourceType,
        @Schema(description = "稳定资源 key", required = true)
        String resourceKey,
        @Schema(description = "资源父节点主键")
        String parentResourceId,
        @Schema(description = "资源所属或引用的页面 key")
        String pageKey,
        @Schema(description = "datasource 在页面内的 key")
        String datasourceKey,
        @Schema(description = "页面路由")
        String routePath,
        @Schema(description = "datasource 的 HTTP 方法")
        String requestMethod,
        @Schema(description = "datasource 的 HTTP 路径模板")
        String requestUrl,
        @Schema(description = "目录展示名称", required = true)
        String displayName,
        @Schema(description = "同级排序值")
        Integer sortOrder,
        @Schema(description = "资源状态", required = true)
        String status
) {

    /**
     * 从持久化目录记录创建接口视图。
     *
     * @param entity 持久化资源记录
     * @return 资源目录视图
     */
    public static PermissionResourceVo from(ConsoleCatalogResourceEntity entity) {
        return new PermissionResourceVo(
                entity.getResourceId(),
                entity.getOwnerPluginId(),
                entity.getModuleKey(),
                CatalogResourceType.valueOf(entity.getResourceType()),
                entity.getResourceKey(),
                entity.getParentResourceId(),
                entity.getPageKey(),
                entity.getDatasourceKey(),
                entity.getRoutePath(),
                entity.getRequestMethod(),
                entity.getRequestUrl(),
                entity.getDisplayName(),
                entity.getSortOrder(),
                entity.getStatus());
    }
}
