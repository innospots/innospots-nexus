package com.innospots.nexus.kernel.user.operator;

/**
 * Thrown when the password fails the strength validator.
 */
public class PasswordWeak extends RuntimeException {

    public PasswordWeak(String message) {
        super(message);
    }
}
