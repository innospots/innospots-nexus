package com.innospots.nexus.console.permission.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 角色或组织单元权限全量替换请求中的一条资源授权。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(name = "PermissionGrantItemRequest", description = "权限授权项")
public record PermissionGrantItemRequest(
        @Schema(description = "被授权的资源主键", required = true)
        String resourceId,
        @Schema(description = "datasource 授权对应的管理端附加查询条件，可为空")
        String constraintDefinition
) {
}
