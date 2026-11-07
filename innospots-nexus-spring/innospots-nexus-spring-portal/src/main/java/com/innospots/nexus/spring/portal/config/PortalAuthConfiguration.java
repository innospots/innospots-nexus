package com.innospots.nexus.spring.portal.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.domain.session.SessionScopeBinder;
import com.innospots.nexus.console.scope.tenant.dao.TenantMemberDao;
import com.innospots.nexus.console.user.portal.service.PortalPasswordService;
import com.innospots.nexus.console.user.portal.service.PortalUserService;
import com.innospots.nexus.portal.auth.adapter.PortalUserDirectory;
import com.innospots.nexus.portal.auth.endpoint.TenantAuthEndpoint;
import com.innospots.nexus.portal.auth.service.TenantAuthFacade;

/**
 * portal 租户认证的 Spring 装配。
 *
 * <p>只负责登录、令牌签发与认证端点；用户能力见 {@link PortalUserConfiguration}，
 * 作用域绑定见 {@link PortalScopeConfiguration}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see TenantAuthEndpoint
 */
@Configuration
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PortalAuthConfiguration {

    /**
     * 登录图形验证码闸门。
     *
     * @param authConfig              认证配置
     * @param captchaChallengeService 验证码挑战服务
     * @return 验证码闸门
     */
    @Bean
    LoginCaptchaGate loginCaptchaGate(
            AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    /**
     * 令牌签发器。
     *
     * @param authConfig 认证配置
     * @return 令牌签发器
     */
    @Bean
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    /**
     * 访问与刷新令牌对签发器。
     *
     * @param tokenIssuer 令牌签发器
     * @return 令牌对签发器
     */
    @Bean
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    /**
     * 租户域登录、令牌刷新与登出编排。
     *
     * @param portalUserDirectory  用户目录
     * @param credentialService    凭证服务
     * @param loginCaptchaGate     验证码闸门
     * @param tenantMemberDao      租户成员 DAO
     * @param passwordDecryptor    密码解密器
     * @param tokenIssuer          令牌签发器
     * @param authTokenPairIssuer  令牌对签发器
     * @param sessionScopeBinder   会话作用域绑定
     * @return 认证门面
     */
    @Bean
    TenantAuthFacade tenantAuthFacade(
            PortalUserDirectory portalUserDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            TenantMemberDao tenantMemberDao,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new TenantAuthFacade(
                portalUserDirectory,
                credentialService,
                loginCaptchaGate,
                tenantMemberDao,
                passwordDecryptor,
                tokenIssuer,
                authTokenPairIssuer,
                sessionScopeBinder);
    }

    /**
     * 租户域认证 REST 资源。
     *
     * @param tenantAuthFacade      认证门面
     * @param portalUserService     用户服务
     * @param portalPasswordService 密码服务
     * @param passwordDecryptor     密码解密器
     * @return 认证端点
     */
    @Bean
    @Lazy
    TenantAuthEndpoint tenantAuthEndpoint(
            TenantAuthFacade tenantAuthFacade,
            PortalUserService portalUserService,
            PortalPasswordService portalPasswordService,
            PasswordDecryptor passwordDecryptor) {
        return new TenantAuthEndpoint(
                tenantAuthFacade, portalUserService, portalPasswordService, passwordDecryptor);
    }
}
