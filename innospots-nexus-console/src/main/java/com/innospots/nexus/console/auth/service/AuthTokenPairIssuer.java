package com.innospots.nexus.console.auth.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * Issues access and refresh token pairs from session scope.
 */
@RequiredArgsConstructor
public class AuthTokenPairIssuer {

    private final TokenIssuer tokenIssuer;

    /**
     * Issues a token pair for the given realm user and session scope.
     *
     * @param realm  PLATFORM or TENANT
     * @param userId platform or tenant user identifier
     * @param scope  session scope encoded into claims
     * @return issued token pair
     */
    public AuthTokenVo issue(SecurityRealm realm, String userId, AuthSessionScope scope) {
        long now = Instant.now().getEpochSecond();
        String accessToken = tokenIssuer.issue(new TokenClaims(
                realm,
                TokenIssuer.PURPOSE_ACCESS,
                scope.tokenType(),
                userId,
                scope.tenantId(),
                scope.tenantMemberId(),
                scope.workspaceId(),
                scope.projectId(),
                now + tokenIssuer.accessTokenTtlSeconds()));
        String refreshToken = tokenIssuer.issue(new TokenClaims(
                realm,
                TokenIssuer.PURPOSE_REFRESH,
                scope.tokenType(),
                userId,
                scope.tenantId(),
                scope.tenantMemberId(),
                scope.workspaceId(),
                scope.projectId(),
                now + tokenIssuer.refreshTokenTtlSeconds()));
        return new AuthTokenVo(
                realm,
                scope.tokenType(),
                accessToken,
                refreshToken,
                scope.tenantId(),
                scope.tenantMemberId(),
                scope.workspaceId(),
                scope.projectId());
    }
}
