package com.innospots.nexus.console.auth.service;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * 从会话作用域签发访问与刷新令牌对。
 *
 * @author Smars
 * @date 2026/09/13
 */
@RequiredArgsConstructor
public class AuthTokenPairIssuer {

    private final TokenIssuer tokenIssuer;

    /**
     * 为给定域用户与会话作用域签发令牌对。
     *
     * @param realm  PLATFORM 或 TENANT
     * @param userId platform or tenant user 标识符
     * @param scope  编码进声明的会话作用域
     * @return issued 令牌对
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
