package com.innospots.nexus.platform.auth.service;

import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.console.auth.domain.request.AuthCaptchaIssueRequest;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.vo.AuthRsaPublicKeyVo;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthRsaKeyService;
import com.innospots.nexus.platform.support.PlatformRequestTrace;

/**
 * 运营管理平台登录与会话令牌编排。
 *
 * <p>Platform 侧注入 {@code platformAuthFacade} 的统一入口；后续 PLATFORM 登录策略（审计、风控等）在此扩展，
 * endpoint 不直接依赖 {@link AuthFacade}。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformPublicAuthEndpoint
 * @see com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint
 */
@Slf4j
public class PlatformAuthService {

    private final AuthFacade authFacade;
    private final AuthRsaKeyService authRsaKeyService;

    public PlatformAuthService(AuthFacade authFacade, AuthRsaKeyService authRsaKeyService) {
        this.authFacade = authFacade;
        this.authRsaKeyService = authRsaKeyService;
    }

    /**
     * 签发登录图形验证码。
     *
     * @param request 可选 clientKey
     * @return 验证码载荷
     */
    public AuthCaptchaVo issueLoginCaptcha(AuthCaptchaIssueRequest request) {
        String clientKey = request == null ? null : request.clientKey();
        log.info(
                "traceId={} platform auth captcha issue clientKeyPresent={}",
                PlatformRequestTrace.currentTraceId(),
                clientKey != null && !clientKey.isBlank());
        return authFacade.issueLoginCaptcha(clientKey);
    }

    /**
     * 返回登录密码 RSA 加密公钥。
     *
     * @return SPKI 公钥 Base64
     */
    public AuthRsaPublicKeyVo getPasswordEncryptionPublicKey() {
        log.debug("traceId={} platform auth RSA public key requested", PlatformRequestTrace.currentTraceId());
        return authRsaKeyService.getPasswordEncryptionPublicKey();
    }

    /**
     * 密码登录并签发令牌对。
     *
     * @param request 登录请求
     * @return 访问与刷新令牌
     */
    public AuthTokenVo login(AuthLoginRequest request) {
        String login = request == null ? null : request.login();
        String traceId = PlatformRequestTrace.currentTraceId();
        log.info("traceId={} platform auth login attempt login={}", traceId, PlatformRequestTrace.safeLogin(login));
        try {
            AuthTokenVo token = authFacade.login(request);
            log.info(
                    "traceId={} platform auth login succeeded login={} tokenType={}",
                    traceId,
                    PlatformRequestTrace.safeLogin(login),
                    token.tokenType());
            return token;
        } catch (RuntimeException ex) {
            log.warn(
                    "traceId={} platform auth login failed login={} reason={}",
                    traceId,
                    PlatformRequestTrace.safeLogin(login),
                    ex.getMessage());
            throw ex;
        }
    }

    /**
     * 使用 refresh token 签发新令牌对（Jersey 层不要求 Bearer；鉴权在 {@link AuthFacade#refresh}）。
     *
     * @param request 刷新请求
     * @return 新令牌对
     */
    public AuthTokenVo refresh(TokenRefreshRequest request) {
        String traceId = PlatformRequestTrace.currentTraceId();
        log.info("traceId={} platform auth token refresh attempt", traceId);
        try {
            AuthTokenVo token = authFacade.refresh(request);
            log.info("traceId={} platform auth token refresh succeeded tokenType={}", traceId, token.tokenType());
            return token;
        } catch (RuntimeException ex) {
            log.warn("traceId={} platform auth token refresh failed reason={}", traceId, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 登出当前会话（MVP：清理服务端会话上下文，不吊销 refresh token）。
     */
    public void logout() {
        log.info("traceId={} platform auth logout", PlatformRequestTrace.currentTraceId());
        authFacade.logout();
    }
}
