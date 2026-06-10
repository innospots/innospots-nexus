package com.innospots.nexus.kernel.user.operator;

/**
 * Thrown when the verification code is invalid, mismatched, or expired.
 */
public class CodeInvalid extends RuntimeException {

    public CodeInvalid(String message) {
        super(message);
    }
}
