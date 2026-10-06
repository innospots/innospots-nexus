package com.innospots.nexus.platform.invite.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformInviteActivateByCodeRequest", description = "凭邀请码注册")
public record PlatformInviteActivateByCodeRequest(
        @Schema(description = "邀请码", required = true)
        String inviteCode,
        @Schema(description = "邮箱或手机，须与邀请一致", required = true)
        String contact,
        @Schema(description = "登录名")
        String loginName,
        @Schema(description = "前端加密密码", required = true)
        String encryptedPassword
) {
}
