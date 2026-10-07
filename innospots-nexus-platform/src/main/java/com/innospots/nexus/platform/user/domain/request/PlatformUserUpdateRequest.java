package com.innospots.nexus.platform.user.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新平台用户可变档案字段的请求。
 */
@Schema(name = "PlatformUserUpdateRequest", description = "更新平台用户请求")
public record PlatformUserUpdateRequest(
        @Schema(description = "显示名称", maxLength = 128)
        String displayName,
        @Schema(description = "邮箱地址", format = "email")
        String email,
        @Schema(description = "手机号")
        String mobile,
        @Schema(description = "内部员工编号")
        String employeeNo
) {
}
