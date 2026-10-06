package com.innospots.nexus.platform.user.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;

/**
 * 变更平台用户生命周期状态的请求。
 */
@Schema(name = "PlatformUserStatusUpdateRequest", description = "更新平台用户状态请求")
public record PlatformUserStatusUpdateRequest(
        @Schema(description = "目标用户状态", required = true)
        PlatformUserStatus status
) {
}
