package com.innospots.nexus.platform.access.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformRegistrationOtpRequest", description = "注册流程获取验证码")
public record PlatformRegistrationOtpRequest(
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机")
        String mobile,
        @Schema(description = "邮箱或手机（与 email/mobile 二选一；若同时提供 email/mobile 则忽略）")
        String contact,
        @Schema(description = "模板语言")
        String locale
) {
}
