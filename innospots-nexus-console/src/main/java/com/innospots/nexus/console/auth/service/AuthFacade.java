package com.innospots.nexus.console.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
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
import com.innospots.nexus.console.scope.service.SessionScopeBinder;

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
    private final AuthTokenPairIssuer tokenPairIssuer;
    private final SessionScopeBinder sessionScopeBinder;

    /**
     * Authenticates a realm user and issues a token pair.
     *
     * @param realm   PLATFORM or TENANT
     * @param request login identity and encrypted password
     * @return issued token pair
     */
    public AuthTokenVo login(SecurityRealm realm, AuthLoginRequest request) {
        Checks.notNull(realm, "realm");
        Checks.notNull(request, "request");
        Checks.notBlank(request.login(), "login");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
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
        AuthSessionScope scope = resolveLoginScope(realm, user.userId());
        AuthTokenVo token = tokenPairIssuer.issue(realm, user.userId(), scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * Exchanges a tenant identity for a business token bound to one membership.
     *
     * @param tenantUserId tenant-realm user identifier
     * @param request      tenant to activate
     * @return TENANT business token
     */
    public AuthTokenVo selectTenant(String tenantUserId, SelectTenantRequest request) {
        Checks.notBlank(tenantUserId, "tenantUserId");
        Checks.notNull(request, "request");
        Checks.notBlank(request.tenantId(), "tenantId");
        TenantMembership membership = membershipDirectory.listActiveMemberships(tenantUserId).stream()
                .filter(item -> request.tenantId().equals(item.tenantId()))
                .findFirst()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        AuthUser user = new AuthUser(tenantUserId, tenantUserId, "ACTIVE", SecurityRealm.TENANT);
        AuthSessionScope scope = new AuthSessionScope(
                TOKEN_TYPE_BUSINESS, membership.tenantId(), membership.tenantMemberId(), null, null);
        AuthTokenVo token = tokenPairIssuer.issue(SecurityRealm.TENANT, tenantUserId, scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * Issues a new token pair from a same-realm refresh token.
     *
     * @param realm   expected realm
     * @param request refresh token
     * @return new token pair
     */
    public AuthTokenVo refresh(SecurityRealm realm, TokenRefreshRequest request) {
        Checks.notNull(realm, "realm");
        Checks.notNull(request, "request");
        Checks.notBlank(request.refreshToken(), "refreshToken");
        TokenClaims claims = tokenIssuer.parse(request.refreshToken());
        if (claims.realm() != realm
                || !TokenIssuer.PURPOSE_REFRESH.equals(claims.purpose())
                || claims.expiresAt() <= Instant.now().getEpochSecond()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        AuthSessionScope scope = new AuthSessionScope(
                claims.tokenType(),
                claims.tenantId(),
                claims.tenantMemberId(),
                claims.workspaceId(),
                claims.projectId());
        AuthTokenVo token = tokenPairIssuer.issue(realm, claims.userId(), scope);
        sessionScopeBinder.bindFromClaims(claims);
        return token;
    }

    /**
     * Completes logout. Compact tokens remain valid until expiry.
     */
    public void logout() {
        sessionScopeBinder.clear();
        log.debug("Logout requested for a compact token session");
    }

    private AuthSessionScope resolveLoginScope(SecurityRealm realm, String userId) {
        if (realm == SecurityRealm.PLATFORM) {
            return new AuthSessionScope(TOKEN_TYPE_BUSINESS, null, null, null, null);
        }
        List<TenantMembership> memberships = membershipDirectory.listActiveMemberships(userId);
        if (memberships.size() == 1) {
            TenantMembership membership = memberships.getFirst();
            return new AuthSessionScope(
                    TOKEN_TYPE_BUSINESS, membership.tenantId(), membership.tenantMemberId(), null, null);
        }
        return new AuthSessionScope(TOKEN_TYPE_IDENTITY, null, null, null, null);
    }
}
