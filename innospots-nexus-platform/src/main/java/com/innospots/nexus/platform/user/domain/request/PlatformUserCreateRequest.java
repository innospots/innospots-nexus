package com.innospots.nexus.platform.user.domain.request;

/**
 * Admin request to create a platform user with a local password.
 *
 * @param loginName         unique login name in the platform realm
 * @param displayName       display name shown in the ops console
 * @param email             email address
 * @param mobile            mobile phone number
 * @param employeeNo        internal employee number
 * @param encryptedPassword frontend encrypted password payload
 */
public record PlatformUserCreateRequest(
        String loginName,
        String displayName,
        String email,
        String mobile,
        String employeeNo,
        String encryptedPassword
) {
}
