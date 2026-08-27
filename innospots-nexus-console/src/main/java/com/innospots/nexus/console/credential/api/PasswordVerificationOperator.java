package com.innospots.nexus.console.credential.api;

import com.innospots.nexus.console.credential.domain.enums.VerificationType;

/**
 * Password verification-code service port used for reset flows.
 */
public interface PasswordVerificationOperator {

    /**
     * Sends a verification code to the given identity.
     *
     * @param identity target email or mobile
     * @param type     transport channel
     */
    void sendVerificationCode(String identity, VerificationType type);

    /**
     * Verifies that the provided code matches the most recent code sent.
     *
     * @param identity target identifier
     * @param type     transport channel
     * @param code     user-supplied code
     * @return true when the code is valid and unexpired
     */
    boolean verifyVerificationCode(String identity, VerificationType type, String code);

    /**
     * Marks a verification code as consumed.
     *
     * @param identity target identifier
     * @param type     transport channel
     */
    void expireVerificationCode(String identity, VerificationType type);
}
