package com.innospots.nexus.console.credential;

import com.innospots.nexus.console.credential.api.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.domain.enums.VerificationType;

/**
 * Null implementation that rejects all verification-code operations.
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
