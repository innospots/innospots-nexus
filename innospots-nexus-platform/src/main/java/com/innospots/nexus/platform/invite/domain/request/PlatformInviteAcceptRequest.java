package com.innospots.nexus.platform.invite.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformInviteAcceptRequest", description = "接受平台邀请")
public record PlatformInviteAcceptRequest(
        @Schema(description = "登录名；可覆盖预置值")
        String loginName,
        @Schema(description = "前端加密密码", required = true)
        String encryptedPassword,
        @Schema(description = "身份验证码（在线模式）")
        String verificationCode,
        @Schema(description = "模板语言")
        String locale
) {
}
