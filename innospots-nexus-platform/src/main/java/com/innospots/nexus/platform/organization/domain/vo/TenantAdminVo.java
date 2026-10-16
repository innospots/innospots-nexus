package com.innospots.nexus.platform.organization.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 租户管理员列表项。
 *
 * @param tenantAdminId 管理员记录 ID
 * @param tenantId      租户 ID
 * @param email         邮箱
 * @param adminRole     角色（{@link com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole}）
 * @param tenantUserId  租户用户 ID
 * @param displayName   展示名
 */
@Schema(name = "TenantAdminVo", description = "租户管理员")
public record TenantAdminVo(
        @Schema(description = "管理员记录 ID")
        String tenantAdminId,
        @Schema(description = "租户 ID")
        String tenantId,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "角色：OWNER 或 ADMINISTRATOR")
        String adminRole,
        @Schema(description = "租户用户 ID")
        String tenantUserId,
        @Schema(description = "展示名")
        String displayName
) {
}
