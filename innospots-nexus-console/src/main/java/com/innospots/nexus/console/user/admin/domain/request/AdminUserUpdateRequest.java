package com.innospots.nexus.console.user.admin.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新平台用户可变档案字段的请求。
 */
@Schema(name = "AdminUserUpdateRequest", description = "更新平台用户请求")
public record AdminUserUpdateRequest(
        @Schema(description = "显示名称", maxLength = 128)
        String displayName,
        @Schema(description = "邮箱地址", format = "email")
        String email,
        @Schema(description = "手机号")
        String mobile
) {
}
