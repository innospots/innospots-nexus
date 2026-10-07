package com.innospots.nexus.console.auth.service;

import java.time.Instant;
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
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;

/**
 * 运维平台域登录与令牌刷新编排。租户域认证见 {@code portal.auth.service.TenantAuthFacade}。
 */
@Slf4j
public class AuthFacade {

    public static final String TOKEN_TYPE_IDENTITY = "IDENTITY";
    public static final String TOKEN_TYPE_BUSINESS = "BUSINESS";

    private final SecurityRealm realm;
    private final UserDirectory userDirectory;
    private final CredentialService credentialService;
    private final LoginCaptchaGate loginCaptchaGate;
    private final PasswordDecryptor passwordDecryptor;
    private final TokenIssuer tokenIssuer;
    private final AuthTokenPairIssuer tokenPairIssuer;
    private final SessionScopeBinder sessionScopeBinder;

    public AuthFacade(
            SecurityRealm realm,
            UserDirectory userDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer tokenPairIssuer,
            SessionScopeBinder sessionScopeBinder
    ) {
        this.realm = Objects.requireNonNull(realm, "realm");
        this.userDirectory = Objects.requireNonNull(userDirectory, "userDirectory");
        this.credentialService = Objects.requireNonNull(credentialService, "credentialService");
        this.loginCaptchaGate = Objects.requireNonNull(loginCaptchaGate, "loginCaptchaGate");
        this.passwordDecryptor = Objects.requireNonNull(passwordDecryptor, "passwordDecryptor");
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer, "tokenIssuer");
        this.tokenPairIssuer = Objects.requireNonNull(tokenPairIssuer, "tokenPairIssuer");
        this.sessionScopeBinder = Objects.requireNonNull(sessionScopeBinder, "sessionScopeBinder");
        if (realm != SecurityRealm.PLATFORM) {
            throw new IllegalArgumentException("AuthFacade supports PLATFORM realm only");
        }
    }

    /**
     * 所属安全域（构造时绑定）。
     */
    public SecurityRealm realm() {
        return realm;
    }

    /**
     * 发放登录用图形验证码。
     *
     * @param clientKey 客户端事务键；空白时由服务端生成
     */
    public AuthCaptchaVo issueLoginCaptcha(String clientKey) {
        return loginCaptchaGate.issueForLogin(realm, clientKey);
    }

    /**
     * 认证域用户并签发令牌对。
     *
     * @param request 登录身份与加密密码
     * @return issued 令牌对
     */
    public AuthTokenVo login(AuthLoginRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.login(), "login");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        loginCaptchaGate.verifyIfRequired(realm, request);
        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        AuthUser user = userDirectory.findByLogin(request.login()).orElse(null);
        if (user == null) {
            log.debug("Login rejected: unknown identity in realm {}", realm);
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        credentialService.authenticate(realm, user.userId(), rawPassword);
        AuthSessionScope scope = new AuthSessionScope(TOKEN_TYPE_BUSINESS, null, null, null, null);
        AuthTokenVo token = tokenPairIssuer.issue(realm, user.userId(), scope);
        sessionScopeBinder.bindAfterAuth(user, scope);
        return token;
    }

    /**
     * 从同域刷新令牌签发新令牌对。
     *
     * @param request 刷新令牌
     * @return new 令牌对
     */
    public AuthTokenVo refresh(TokenRefreshRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.refreshToken(), "refreshToken");
        TokenClaims claims = tokenIssuer.parse(request.refreshToken());
        if (claims.realm() != realm
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
        AuthTokenVo token = tokenPairIssuer.issue(realm, claims.userId(), scope);
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
}
