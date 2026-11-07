package com.innospots.nexus.console.user.admin.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;

/**
 * 变更平台用户生命周期状态的请求。
 */
@Schema(name = "AdminUserStatusUpdateRequest", description = "更新平台用户状态请求")
public record AdminUserStatusUpdateRequest(
        @Schema(description = "目标用户状态", required = true)
        AdminUserStatus status
) {
}
