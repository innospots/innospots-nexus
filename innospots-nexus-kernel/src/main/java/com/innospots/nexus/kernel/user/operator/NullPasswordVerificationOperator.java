package com.innospots.nexus.kernel.user.operator;

import com.innospots.nexus.kernel.user.domain.enums.VerificationType;

/**
 * Null implementation that rejects all verification code operations.
 * <p>Awaiting a real implementation to be wired into the application context.
 * Calling any method throws {@link UnsupportedOperationException}.</p>
 */
public class NullPasswordVerificationOperator implements PasswordVerificationOperator {

    @Override
    public void sendVerificationCode(String identity, VerificationType type) {
        throw new UnsupportedOperationException("PasswordVerificationOperator not yet initialized");
    }

    @Override
    public boolean verifyVerificationCode(String identity, VerificationType type, String code) {
        throw new UnsupportedOperationException("PasswordVerificationOperator not yet initialized");
    }

    @Override
    public void expireVerificationCode(String identity, VerificationType type) {
        throw new UnsupportedOperationException("PasswordVerificationOperator not yet initialized");
    }
}
