package com.innospots.nexus.console.auth.domain.vo;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * Token pair returned after login, register, refresh, or tenant selection.
 *
 * @param realm           PLATFORM or TENANT
 * @param tokenType       IDENTITY or BUSINESS
 * @param accessToken     access token
 * @param refreshToken    refresh token
 * @param tenantId        set on TENANT business tokens
 * @param tenantMemberId  set on TENANT business tokens
 */
public record AuthTokenVo(
        SecurityRealm realm,
        String tokenType,
        String accessToken,
        String refreshToken,
        String tenantId,
        String tenantMemberId
) {
}
