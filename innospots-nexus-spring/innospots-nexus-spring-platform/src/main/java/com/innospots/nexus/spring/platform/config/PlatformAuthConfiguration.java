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
import com.innospots.nexus.platform.auth.endpoint.PlatformAuthEndpoint;
import com.innospots.nexus.platform.auth.operator.PlatformPasswordOperator;
import com.innospots.nexus.platform.scope.PlatformSessionScopeBinder;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;

/**
 * platform 运维域认证与用户相关 Spring 装配。
 *
 * @author Smars
 * @date 2026/09/23
 * @see PlatformAuthEndpoint
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.platform.user.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PlatformAuthConfiguration {

    @Bean
    PlatformUserDirectory platformUserDirectory(PlatformUserDao platformUserDao) {
        return new PlatformUserDirectory(platformUserDao);
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
    PlatformPasswordOperator platformPasswordOperator(
            PlatformUserDao platformUserDao,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator) {
        return new PlatformPasswordOperator(platformUserDao, credentialService, passwordVerificationOperator);
    }

    @Bean
    @Lazy
    PlatformAuthEndpoint platformAuthEndpoint(
            @Qualifier("platformAuthFacade") AuthFacade authFacade,
            PlatformPasswordOperator platformPasswordOperator,
            PasswordDecryptor passwordDecryptor) {
        return new PlatformAuthEndpoint(authFacade, platformPasswordOperator, passwordDecryptor);
    }
}
