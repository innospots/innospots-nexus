package com.innospots.nexus.kernel.user.domain.request;

/**
 * Request object for registering a user with local password credentials.
 *
 * @param userName          unique login user name
 * @param displayName       display name shown in UI
 * @param realName          legal or real-world name when provided
 * @param email             email address
 * @param mobile            mobile phone number
 * @param encryptedPassword frontend encrypted password payload
 */
public record UserPasswordRegisterRequest(
        String userName,
        String displayName,
        String realName,
        String email,
        String mobile,
        String encryptedPassword
) {
}
