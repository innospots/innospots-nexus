package com.innospots.nexus.kernel.user.domain.request;

import java.time.LocalDateTime;

/**
 * Request object for registering a tenant-realm user with an external OAuth identity.
 *
 * @param userName            unique login user name
 * @param displayName         display name shown in UI
 * @param email               email address
 * @param mobile              mobile phone number
 * @param region              region preference such as CN
 * @param timeZone            IANA time zone such as Asia/Shanghai
 * @param language            UI language such as zh-CN
 * @param provider            OAuth provider code
 * @param providerSubject     stable subject identifier from the provider
 * @param providerAccount     provider-side account name
 * @param providerDisplayName provider-side display name
 * @param providerEmail       provider-side email
 * @param providerAvatarUrl   provider-side avatar URL
 * @param accessTokenKey      secure storage key for the access token
 * @param refreshTokenKey     secure storage key for the refresh token
 * @param tokenExpiresAt      access token expiry time
 */
public record UserOauthRegisterRequest(
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String provider,
        String providerSubject,
        String providerAccount,
        String providerDisplayName,
        String providerEmail,
        String providerAvatarUrl,
        String accessTokenKey,
        String refreshTokenKey,
        LocalDateTime tokenExpiresAt
) {
}
