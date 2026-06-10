package com.innospots.nexus.kernel.user.operator;

/**
 * Password verification code service port.
 * <p>Implementations are responsible for sending, verifying, and expiring
 * verification codes used for password reset.  The default null
 * implementation ({@link NullPasswordVerificationOperator}) throws
 * {@link UnsupportedOperationException} to signal that verification is
 * not yet wired up.</p>
 */
public interface PasswordVerificationOperator {

    /**
     * Sends a verification code to the given identity.
     *
     * @param identity the target identifier (email address or mobile number,
     *                 depending on transport type)
     * @param type     the transport channel along which the code was sent
     */
    void sendVerificationCode(String identity, VerificationType type);

    /**
     * Verifies that the provided code matches the most recent code sent to
     * the given identity via the specified transport type.
     *
     * @param identity     the target identifier
     * @param type         the transport channel used for sending the code
     * @param code         the code provided by the user
     * @return {@code true} if the code is valid and not yet expired;
     *         {@code false} if the code is invalid, mismatched, or expired
     */
    boolean verifyVerificationCode(String identity, VerificationType type, String code);

    /**
     * Marks a verification code as consumed so it cannot be reused.
     *
     * @param identity the target identifier
     * @param type     the transport channel
     */
    void expireVerificationCode(String identity, VerificationType type);
}
