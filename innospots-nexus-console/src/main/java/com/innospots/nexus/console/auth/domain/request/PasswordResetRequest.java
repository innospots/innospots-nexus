package com.innospots.nexus.console.auth.domain.request;

import com.innospots.nexus.console.credential.domain.enums.VerificationType;

/**
 * Password reset using a verification code.
 *
 * @param identity             user_name, email, or mobile
 * @param verificationCode     one-time code
 * @param type                 EMAIL or MOBILE
 * @param newEncryptedPassword frontend encrypted new password
 */
public record PasswordResetRequest(
        String identity,
        String verificationCode,
        VerificationType type,
        String newEncryptedPassword
) {
}
