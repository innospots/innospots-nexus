package com.innospots.nexus.console.permission.domain.request;

/** 角色或组织单元权限全量替换请求中的一条资源授权。 */
public record PermissionGrantItemRequest(
        /** 被授权的资源主键。 */
        String resourceId,
        /** datasource 授权对应的管理端附加查询条件，可为空。 */
        String constraintDefinition
) {
}
