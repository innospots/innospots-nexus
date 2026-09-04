package com.innospots.nexus.console.permission.domain.vo;

import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.core.plugin.contribution.console.catalog.domain.enums.CatalogResourceType;

/** 面向管理端和前端的权限资源目录视图。 */
public record PermissionResourceVo(
        /** 资源记录主键。 */
        String resourceId,
        /** 来源插件稳定身份。 */
        String ownerPluginId,
        /** 所属模块 key。 */
        String moduleKey,
        /** 资源类型。 */
        CatalogResourceType resourceType,
        /** 稳定资源 key。 */
        String resourceKey,
        /** 资源父节点主键。 */
        String parentResourceId,
        /** 资源所属或引用的页面 key。 */
        String pageKey,
        /** datasource 在页面内的 key。 */
        String datasourceKey,
        /** 页面路由。 */
        String routePath,
        /** datasource 的 HTTP 方法。 */
        String requestMethod,
        /** datasource 的 HTTP 路径模板。 */
        String requestUrl,
        /** 目录展示名称。 */
        String displayName,
        /** 同级排序值。 */
        Integer sortOrder,
        /** 资源状态。 */
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
