package com.innospots.nexus.platform.invite.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformInviteOtpRequest", description = "邀请注册页获取验证码")
public record PlatformInviteOtpRequest(
        @Schema(description = "模板语言")
        String locale
) {
}
