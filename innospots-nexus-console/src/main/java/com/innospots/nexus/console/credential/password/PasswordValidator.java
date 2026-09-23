package com.innospots.nexus.console.credential.password;

/**
 * 密码强度校验器。
 * <p>要求：最小长度，且必须包含大写字母、小写字母与数字。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.console.credential.password.service.CredentialService
 */
public class PasswordValidator {

    public static final int MIN_LENGTH = 8;

    /**
     * 当密码满足全部强度要求时返回 true。
     *
     * @param password 待校验密码
     * @return 有效时为 {@code true}，否则为 {@code false}
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
