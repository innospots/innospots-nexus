package com.innospots.nexus.console.auth.domain.request;

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
public record PasswordResetRequest(
        String identity,
        String verificationCode,
        VerificationType type,
        String newEncryptedPassword
) {
}
