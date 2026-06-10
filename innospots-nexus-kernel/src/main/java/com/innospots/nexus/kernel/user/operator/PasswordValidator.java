package com.innospots.nexus.kernel.user.operator;

/**
 * Password strength validator.
 * <p>Requirements: minimum length, must contain uppercase, lowercase, and digit.</p>
 */
public class PasswordValidator {

    public static final int MIN_LENGTH = 8;

    /**
     * Returns true when the password meets all strength requirements.
     *
     * @param password the password to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasUpper && hasLower && hasDigit) {
                return true;
            }
        }
        return hasUpper && hasLower && hasDigit;
    }
}
