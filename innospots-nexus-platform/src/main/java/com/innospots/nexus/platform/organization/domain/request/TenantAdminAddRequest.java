package com.innospots.nexus.platform.organization.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 按邮箱添加租户管理员。
 *
 * @param email     租户用户邮箱
 * @param adminRole 角色（{@link com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole}）
 */
@Schema(name = "TenantAdminAddRequest", description = "添加租户管理员")
public record TenantAdminAddRequest(
        @Schema(description = "租户用户邮箱", required = true)
        String email,
        @Schema(description = "角色：OWNER（所有者）或 ADMINISTRATOR（管理员）", required = true)
        String adminRole
) {
}
