package com.innospots.nexus.platform.access.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformAccessRegistrationSubmitRequest", description = "注册待审核模式提交")
public record PlatformAccessRegistrationSubmitRequest(
        @Schema(description = "登录名", required = true)
        String loginName,
        @Schema(description = "显示名称")
        String displayName,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机")
        String mobile,
        @Schema(description = "申请说明")
        String description,
        @Schema(description = "前端加密密码", required = true)
        String encryptedPassword,
        @Schema(description = "身份验证码", required = true)
        String verificationCode,
        @Schema(description = "模板语言")
        String locale
) {
}
