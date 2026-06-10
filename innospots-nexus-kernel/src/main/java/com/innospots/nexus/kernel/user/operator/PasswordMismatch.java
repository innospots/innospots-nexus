package com.innospots.nexus.kernel.user.operator;

/**
 * Thrown when the supplied old password does not match the stored hash.
 */
public class PasswordMismatch extends RuntimeException {

    public PasswordMismatch(String message) {
        super(message);
    }
}
