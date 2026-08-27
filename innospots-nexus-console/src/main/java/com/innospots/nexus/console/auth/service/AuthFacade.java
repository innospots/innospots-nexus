package com.innospots.nexus.console.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.api.CredentialStore;
import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.auth.domain.model.TenantMembership;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.credential.api.PasswordDecryptor;

/**
 * Orchestrates realm login, tenant selection, and token refresh.
 * User rows stay in platform or kernel; this facade only uses directory ports.
 */
@Slf4j
@RequiredArgsConstructor
public class AuthFacade {

    public static final String TOKEN_TYPE_IDENTITY = "IDENTITY";
    public static final String TOKEN_TYPE_BUSINESS = "BUSINESS";

    private final UserDirectory userDirectory;
    private final CredentialStore credentialStore;
    private final MembershipDirectory membershipDirectory;
    private final PasswordDecryptor passwordDecryptor;
    private final TokenIssuer tokenIssuer;

    /**
     * Authenticates a realm user and issues a token pair.
     *
     * @param realm   PLATFORM or TENANT
     * @param request login identity and encrypted password
     * @return issued token pair
     */
    public AuthTokenVo login(SecurityRealm realm, AuthLoginRequest request) {
        Objects.requireNonNull(realm, "realm must not be null");
        if (request == null || blank(request.login()) || blank(request.encryptedPassword())) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        AuthUser user = userDirectory.findByLogin(realm, request.login())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.USER_NOT_FOUND));
        CredentialRecord credential = credentialStore.findPassword(realm, user.userId())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.PASSWORD_ERROR));
        if (credential.lockedUntil() != null && credential.lockedUntil().isAfter(LocalDateTime.now())) {
            throw NexusException.build(NexusStatusCode.PASSWORD_ERROR);
        }
        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        if (!CryptoUtils.matchesPassword(rawPassword, credential.passwordHash())) {
            int failed = credential.failedAttempts() == null ? 1 : credential.failedAttempts() + 1;
            credentialStore.updatePassword(realm, new CredentialRecord(
                    credential.userId(),
                    credential.passwordHash(),
                    credential.passwordSalt(),
                    credential.passwordAlgorithm(),
                    failed,
                    credential.lockedUntil(),
                    credential.forceReset()));
            throw NexusException.build(NexusStatusCode.PASSWORD_ERROR);
        }
        credentialStore.updatePassword(realm, new CredentialRecord(
                credential.userId(),
                credential.passwordHash(),
                credential.passwordSalt(),
                credential.passwordAlgorithm(),
                0,
                null,
                credential.forceReset()));
        return issuePair(realm, user.userId(), resolveLoginScope(realm, user.userId()));
    }

    /**
     * Exchanges a tenant identity for a business token bound to one membership.
     *
     * @param tenantUserId tenant-realm user identifier
     * @param request      tenant to activate
     * @return TENANT business token
     */
    public AuthTokenVo selectTenant(String tenantUserId, SelectTenantRequest request) {
        if (blank(tenantUserId) || request == null || blank(request.tenantId())) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        TenantMembership membership = membershipDirectory.listActiveMemberships(tenantUserId).stream()
                .filter(item -> request.tenantId().equals(item.tenantId()))
                .findFirst()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        return issuePair(SecurityRealm.TENANT, tenantUserId, new LoginScope(
                TOKEN_TYPE_BUSINESS, membership.tenantId(), membership.tenantMemberId()));
    }

    /**
     * Issues a new token pair from a same-realm refresh token.
     *
     * @param realm   expected realm
     * @param request refresh token
     * @return new token pair
     */
    public AuthTokenVo refresh(SecurityRealm realm, TokenRefreshRequest request) {
        Objects.requireNonNull(realm, "realm must not be null");
        if (request == null || blank(request.refreshToken())) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
        }
        TokenClaims claims = tokenIssuer.parse(request.refreshToken());
        if (claims.realm() != realm
                || !TokenIssuer.PURPOSE_REFRESH.equals(claims.purpose())
                || claims.expiresAt() <= Instant.now().getEpochSecond()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        return issuePair(claims.realm(), claims.userId(), new LoginScope(
                claims.tokenType(), claims.tenantId(), claims.tenantMemberId()));
    }

    /**
     * Completes logout. Compact tokens remain valid until expiry.
     */
    public void logout() {
        log.debug("Logout requested for a compact token session");
    }

    private LoginScope resolveLoginScope(SecurityRealm realm, String userId) {
        if (realm == SecurityRealm.PLATFORM) {
            return new LoginScope(TOKEN_TYPE_BUSINESS, null, null);
        }
        List<TenantMembership> memberships = membershipDirectory.listActiveMemberships(userId);
        if (memberships.size() == 1) {
            TenantMembership membership = memberships.getFirst();
            return new LoginScope(TOKEN_TYPE_BUSINESS, membership.tenantId(), membership.tenantMemberId());
        }
        return new LoginScope(TOKEN_TYPE_IDENTITY, null, null);
    }

    private AuthTokenVo issuePair(SecurityRealm realm, String userId, LoginScope scope) {
        long now = Instant.now().getEpochSecond();
        String accessToken = tokenIssuer.issue(new TokenClaims(
                realm,
                TokenIssuer.PURPOSE_ACCESS,
                scope.tokenType(),
                userId,
                scope.tenantId(),
                scope.tenantMemberId(),
                now + tokenIssuer.accessTokenTtlSeconds()));
        String refreshToken = tokenIssuer.issue(new TokenClaims(
                realm,
                TokenIssuer.PURPOSE_REFRESH,
                scope.tokenType(),
                userId,
                scope.tenantId(),
                scope.tenantMemberId(),
                now + tokenIssuer.refreshTokenTtlSeconds()));
        return new AuthTokenVo(
                realm,
                scope.tokenType(),
                accessToken,
                refreshToken,
                scope.tenantId(),
                scope.tenantMemberId());
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private record LoginScope(String tokenType, String tenantId, String tenantMemberId) {
    }
}
