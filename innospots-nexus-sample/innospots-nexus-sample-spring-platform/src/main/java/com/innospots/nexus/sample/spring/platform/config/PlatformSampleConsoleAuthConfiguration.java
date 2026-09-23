package com.innospots.nexus.sample.spring.platform.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.RsaPasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.platform.scope.PlatformSessionScopeBinder;
import com.innospots.nexus.platform.auth.adapter.PlatformUserDirectory;
import com.innospots.nexus.platform.auth.endpoint.PlatformAuthEndpoint;
import com.innospots.nexus.platform.auth.operator.PlatformPasswordOperator;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.sample.spring.platform.scope.SamplePlatformScopeSupport;

/**
 * 运营平台示例：{@code console.auth} 与 platform 认证端点装配（不依赖 kernel）。
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.platform.user.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PlatformSampleConsoleAuthConfiguration {

    @Bean
    AuthConfig authConfig(
            @Value("${nexus.console.auth.token-secret:}") String tokenSecret,
            @Value("${nexus.console.auth.access-token-ttl-seconds:7200}") long accessTtl,
            @Value("${nexus.console.auth.refresh-token-ttl-seconds:604800}") long refreshTtl,
            @Value("${nexus.console.auth.tenant-login-captcha-enabled:true}") boolean tenantLoginCaptchaEnabled,
            @Value("${nexus.console.auth.platform-login-captcha-enabled:true}") boolean platformLoginCaptchaEnabled) {
        AuthConfig config = new AuthConfig();
        if (tokenSecret != null && !tokenSecret.isBlank()) {
            config.setTokenSecret(tokenSecret);
        }
        config.setAccessTokenTtlSeconds(accessTtl);
        config.setRefreshTokenTtlSeconds(refreshTtl);
        config.setTenantLoginCaptchaEnabled(tenantLoginCaptchaEnabled);
        config.setPlatformLoginCaptchaEnabled(platformLoginCaptchaEnabled);
        return config;
    }

    @Bean
    PasswordDecryptor passwordDecryptor(
            @Value("${nexus.console.auth.rsa.private-key}") String privateKey) {
        return new RsaPasswordDecryptor(privateKey);
    }

    @Bean
    PlatformUserDirectory platformUserDirectory(PlatformUserDao platformUserDao) {
        return new PlatformUserDirectory(platformUserDao);
    }

    @Bean
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Bean
    MembershipDirectory membershipDirectory() {
        return SamplePlatformScopeSupport.emptyMembershipDirectory();
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
            MembershipDirectory membershipDirectory,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new AuthFacade(
                SecurityRealm.PLATFORM,
                platformUserDirectory,
                credentialService,
                loginCaptchaGate,
                membershipDirectory,
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
