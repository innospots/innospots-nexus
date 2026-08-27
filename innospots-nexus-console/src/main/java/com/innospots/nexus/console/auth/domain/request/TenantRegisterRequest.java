package com.innospots.nexus.console.auth.domain.request;

/**
 * Tenant-realm identity registration. Does not create a TenantMember.
 *
 * @param userName           unique login name
 * @param displayName        optional display name
 * @param email              optional email
 * @param mobile             optional mobile
 * @param region             optional region such as CN
 * @param timeZone           optional IANA time zone
 * @param language           optional UI language such as zh-CN
 * @param encryptedPassword  frontend encrypted password
 */
public record TenantRegisterRequest(
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
