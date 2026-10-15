package com.innospots.nexus.console.role.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 创建角色请求（不含归属字段；由调用域 service 固化 ownerType / securityRealm）。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleName    显示名称
 * @param roleCode    归属范围内唯一的稳定编码
 * @param description 可选 role 描述
 */
@Schema(name = "RoleCreateRequest", description = "创建角色请求")
public record RoleCreateRequest(
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "归属范围内唯一的稳定编码", required = true)
        String roleCode,
        @Schema(description = "可选角色描述")
        String description
) {
}
