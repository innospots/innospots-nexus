package com.innospots.nexus.platform.user.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 管理员为指定平台用户重置登录密码。
 *
 * @param encryptedPassword 前端 RSA 加密后的新密码
 */
@Schema(name = "PlatformUserPasswordResetRequest", description = "管理员重置平台用户密码")
public record PlatformUserPasswordResetRequest(
        @Schema(description = "前端加密的新密码", required = true)
        String encryptedPassword
) {
}
