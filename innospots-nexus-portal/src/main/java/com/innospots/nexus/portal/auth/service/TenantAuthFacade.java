package com.innospots.nexus.portal.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.portal.auth.api.MembershipDirectory;
import com.innospots.nexus.portal.auth.domain.model.TenantMembership;
import com.innospots.nexus.portal.auth.domain.request.SelectTenantRequest;

/**
 * 租户域登录、租户选择与令牌刷新编排。
 */
@Slf4j
public class TenantAuthFacade {

    private static final SecurityRealm REALM = SecurityRealm.TENANT;

    private final UserDirectory userDirectory;
    private final CredentialService credentialService;
    private final LoginCaptchaGate loginCaptchaGate;
    private final MembershipDirectory membershipDirectory;
    private final PasswordDecryptor passwordDecryptor;
    private final TokenIssuer tokenIssuer;
    private final AuthTokenPairIssuer tokenPairIssuer;
    private final SessionScopeBinder sessionScopeBinder;

    public TenantAuthFacade(
            UserDirectory userDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            MembershipDirectory membershipDirectory,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer tokenPairIssuer,
            SessionScopeBinder sessionScopeBinder
    ) {
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory");
        this.credentialService = Objects.requireNonNull(credentialService, "credentialService");
        this.loginCaptchaGate = Objects.requireNonNull(loginCaptchaGate, "loginCaptchaGate");
        this.membershipDirectory = Objects.requireNonNull(membershipDirectory, "membershipDirectory");
        this.passwordDecryptor = Objects.requireNonNull(passwordDecryptor, "passwordDecryptor");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer");
        this.tokenPairIssuer = Objects.requireNonNull(tokenPairIssuer, "tokenPairIssuer");
        this.sessionScopeBinder = Objects.requireNonNull(sessionScopeBinder, "sessionScopeBinder");
    }

    /**
     * 发放登录用图形验证码。
     */
    public AuthCaptchaVo issueLoginCaptcha(String clientKey) {
        return loginCaptchaGate.issueForLogin(REALM, clientKey);
    }

    /**
     * 认证租户域用户并签发令牌对。
     */
    public AuthTokenVo login(AuthLoginRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.login(), "login");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        loginCaptchaGate.verifyIfRequired(REALM, request);
        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        AuthUser user = userDirectory.findByLogin(request.login()).orElse(null);
        if (user == null) {
            log.debug("Login rejected: unknown identity in realm {}", REALM);
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        credentialService.authenticate(REALM, user.userId(), rawPassword);
        AuthSessionScope scope = resolveLoginScope(user.userId());
        AuthTokenVo token = tokenPairIssuer.issue(REALM, user.userId(), scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * 将租户身份交换为绑定单一成员关系的业务令牌。
     */
    public AuthTokenVo selectTenant(String tenantUserId, SelectTenantRequest request) {
        Checks.notBlank(tenantUserId, "tenantUserId");
        Checks.notNull(request, "request");
        Checks.notBlank(request.tenantId(), "tenantId");
        AuthUser user = userDirectory.findById(tenantUserId)
                .orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
        TenantMembership membership = membershipDirectory.listActiveMemberships(tenantUserId).stream()
                .filter(item -> request.tenantId().equals(item.tenantId()))
                .findFirst()
                .orElseThrow(() -> NexusException.build(NexusStatusCode.NO_PERMISSION));
        AuthSessionScope scope = new AuthSessionScope(
                AuthFacade.TOKEN_TYPE_BUSINESS, membership.tenantId(), membership.tenantMemberId(), null, null);
        AuthTokenVo token = tokenPairIssuer.issue(REALM, tenantUserId, scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * 从租户域刷新令牌签发新令牌对。
     */
    public AuthTokenVo refresh(TokenRefreshRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.refreshToken(), "refreshToken");
        TokenClaims claims = tokenIssuer.parse(request.refreshToken());
        if (claims.realm() != REALM
                || !TokenIssuer.PURPOSE_REFRESH.equals(claims.purpose())
                || claims.expiresAt() <= Instant.now().getEpochSecond()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        AuthUser user = userDirectory.findById(claims.userId())
                .orElseThrow(() -> NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
        AuthSessionScope scope = new AuthSessionScope(
                claims.tokenType(),
                claims.tenantId(),
                claims.tenantMemberId(),
                claims.workspaceId(),
                claims.projectId());
        AuthTokenVo token = tokenPairIssuer.issue(REALM, claims.userId(), scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * 完成登出。紧凑令牌在过期前仍然有效。
     */
    public void logout() {
        sessionScopeBinder.clear();
        log.debug("Logout requested for a compact token session");
    }

    private AuthSessionScope resolveLoginScope(String userId) {
        List<TenantMembership> memberships = membershipDirectory.listActiveMemberships(userId);
        if (memberships.size() == 1) {
            TenantMembership membership = memberships.getFirst();
            return new AuthSessionScope(
                    AuthFacade.TOKEN_TYPE_BUSINESS,
                    membership.tenantId(),
                    membership.tenantMemberId(),
                    null,
                    null);
        }
        return new AuthSessionScope(AuthFacade.TOKEN_TYPE_IDENTITY, null, null, null, null);
    }
}
