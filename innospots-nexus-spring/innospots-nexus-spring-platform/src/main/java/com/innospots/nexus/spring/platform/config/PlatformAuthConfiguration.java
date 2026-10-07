package com.innospots.nexus.spring.platform.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory;
import com.innospots.nexus.platform.auth.support.PlatformUserIdentityResolver;
import com.innospots.nexus.platform.auth.endpoint.PlatformAuthSessionEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicAuthEndpoint;
import com.innospots.nexus.platform.auth.endpoint.PlatformPublicPasswordResetEndpoint;
import com.innospots.nexus.platform.auth.service.PlatformAuthService;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.scope.PlatformSessionScopeBinder;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;

/**
 * 运营管理平台认证与用户相关 Spring 装配。
 *
 * @author Smars
 * @date 2026/09/23
 * @see PlatformPublicAuthEndpoint
 * @see PlatformAuthSessionEndpoint
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.platform.user.dao",
            "com.innospots.nexus.platform.invite.dao",
            "com.innospots.nexus.platform.access.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PlatformAuthConfiguration {

    @Bean
    PlatformUserIdentityResolver platformUserIdentityResolver(PlatformUserDao platformUserDao) {
        return new PlatformUserIdentityResolver(platformUserDao);
    }

    @Bean
    PlatformUserDirectory platformUserDirectory(
            PlatformUserDao platformUserDao,
            PlatformUserIdentityResolver platformUserIdentityResolver) {
        return new PlatformUserDirectory(platformUserDao, platformUserIdentityResolver);
    }

    @Bean
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Bean
    SessionScopeBinder sessionScopeBinder() {
        return new PlatformSessionScopeBinder();
    }

    @Bean
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Bean
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    @Bean(name = "platformAuthFacade")
    AuthFacade platformAuthFacade(
            PlatformUserDirectory platformUserDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new AuthFacade(
                SecurityRealm.PLATFORM,
                platformUserDirectory,
                credentialService,
                loginCaptchaGate,
                passwordDecryptor,
                tokenIssuer,
                authTokenPairIssuer,
                sessionScopeBinder);
    }

    @Bean
    PlatformAuthService platformAuthService(@Qualifier("platformAuthFacade") AuthFacade authFacade) {
        return new PlatformAuthService(authFacade);
    }

    @Bean
    PlatformPasswordService platformPasswordService(
            PlatformUserOperator platformUserOperator,
            PlatformUserIdentityResolver platformUserIdentityResolver,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator,
            PasswordDecryptor passwordDecryptor) {
        return new PlatformPasswordService(
                platformUserOperator,
                platformUserIdentityResolver,
                credentialService,
                passwordVerificationOperator,
                passwordDecryptor);
    }

    @Bean
    @Lazy
    PlatformPublicAuthEndpoint platformPublicAuthEndpoint(PlatformAuthService platformAuthService) {
        return new PlatformPublicAuthEndpoint(platformAuthService);
    }

    @Bean
    @Lazy
    PlatformPublicPasswordResetEndpoint platformPublicPasswordResetEndpoint(
            PlatformPasswordService platformPasswordService) {
        return new PlatformPublicPasswordResetEndpoint(platformPasswordService);
    }

    @Bean
    @Lazy
    PlatformAuthSessionEndpoint platformAuthSessionEndpoint(
            PlatformAuthService platformAuthService,
            PlatformPasswordService platformPasswordService) {
        return new PlatformAuthSessionEndpoint(platformAuthService, platformPasswordService);
    }
}
