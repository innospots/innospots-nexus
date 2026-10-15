package com.innospots.nexus.console.role.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 创建由平台、租户或工作区节点拥有的角色的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleName    显示名称
 * @param roleCode    归属范围内唯一的稳定编码
 * @param ownerType   归属层级（同时决定安全域）
 * @param ownerId     owner 标识符; empty for PLATFORM
 * @param description 可选 role 描述
 */
@Schema(name = "RoleCreateRequest", description = "创建角色请求")
public record RoleCreateRequest(
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "归属范围内唯一的稳定编码", required = true)
        String roleCode,
        @Schema(description = "归属层级", required = true)
        RoleOwnerType ownerType,
        @Schema(description = "owner 标识符；PLATFORM 时为空")
        String ownerId,
        @Schema(description = "可选角色描述")
        String description
) {
}
