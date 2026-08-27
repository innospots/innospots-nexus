package com.innospots.nexus.kernel.user.domain.request;

/**
 * Request object for registering a tenant-realm user with local password credentials.
 *
 * @param userName          unique login user name
 * @param displayName       display name shown in UI
 * @param email             email address
 * @param mobile            mobile phone number
 * @param region            region preference such as CN
 * @param timeZone          IANA time zone such as Asia/Shanghai
 * @param language          UI language such as zh-CN
 * @param encryptedPassword frontend encrypted password payload
 */
public record UserPasswordRegisterRequest(
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String encryptedPassword
) {
}
