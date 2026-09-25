package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

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
import com.innospots.nexus.platform.auth.operator.PlatformPasswordOperator;
import com.innospots.nexus.platform.scope.PlatformSessionScopeBinder;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;

/**
 * platform 运维域认证与用户相关 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PlatformAuthBeans {

    @Produces
    @Singleton
    PlatformUserDirectory platformUserDirectory(PlatformUserDao platformUserDao) {
        return new PlatformUserDirectory(platformUserDao);
    }

    @Produces
    @Singleton
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Produces
    @Singleton
    SessionScopeBinder sessionScopeBinder() {
        return new PlatformSessionScopeBinder();
    }

    @Produces
    @Singleton
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Produces
    @Singleton
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    @Produces
    @Singleton
    @Named("platformAuthFacade")
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

    @Produces
    @Singleton
    PlatformPasswordOperator platformPasswordOperator(
            PlatformUserDao platformUserDao,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator) {
        return new PlatformPasswordOperator(platformUserDao, credentialService, passwordVerificationOperator);
    }
}
