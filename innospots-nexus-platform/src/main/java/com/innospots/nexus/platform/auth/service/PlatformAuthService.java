package com.innospots.nexus.platform.auth.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.auth.domain.request.AuthCaptchaIssueRequest;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.service.AuthFacade;

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
@RequiredArgsConstructor
public class PlatformAuthService {

    private final AuthFacade authFacade;

    /**
     * 签发登录图形验证码。
     *
     * @param request 可选 clientKey
     * @return 验证码载荷
     */
    public AuthCaptchaVo issueLoginCaptcha(AuthCaptchaIssueRequest request) {
        String clientKey = request == null ? null : request.clientKey();
        return authFacade.issueLoginCaptcha(clientKey);
    }

    /**
     * 密码登录并签发令牌对。
     *
     * @param request 登录请求
     * @return 访问与刷新令牌
     */
    public AuthTokenVo login(AuthLoginRequest request) {
        return authFacade.login(request);
    }

    /**
     * 使用 refresh token 签发新令牌对（Jersey 层不要求 Bearer；鉴权在 {@link AuthFacade#refresh}）。
     *
     * @param request 刷新请求
     * @return 新令牌对
     */
    public AuthTokenVo refresh(TokenRefreshRequest request) {
        return authFacade.refresh(request);
    }

    /**
     * 登出当前会话（MVP：清理服务端会话上下文，不吊销 refresh token）。
     */
    public void logout() {
        authFacade.logout();
    }
}
