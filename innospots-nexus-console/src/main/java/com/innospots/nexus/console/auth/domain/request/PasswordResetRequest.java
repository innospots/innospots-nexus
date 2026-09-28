package com.innospots.nexus.console.auth.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.credential.password.VerificationType;

/**
 * 使用验证码重置密码。
 *
 * @author Smars
 * @date 2026/09/13
 * @param identity             user_name、email 或 mobile
 * @param verificationCode     一次性验证码
 * @param type                 EMAIL 或 MOBILE
 * @param newEncryptedPassword 前端加密的新密码
 */
@Schema(name = "PasswordResetRequest", description = "验证码重置密码请求")
public record PasswordResetRequest(
        @Schema(description = "用户名、邮箱或手机号", required = true)
        String identity,
        @Schema(description = "一次性验证码", required = true)
        String verificationCode,
        @Schema(description = "验证渠道", required = true)
        VerificationType type,
        @Schema(description = "新前端加密密码", required = true)
        String newEncryptedPassword
) {
}
