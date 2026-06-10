package com.innospots.nexus.kernel.user.operator;

/**
 * Thrown when the new password is identical to the old password.
 */
public class PasswordSame extends RuntimeException {

    public PasswordSame(String message) {
        super(message);
    }
}
