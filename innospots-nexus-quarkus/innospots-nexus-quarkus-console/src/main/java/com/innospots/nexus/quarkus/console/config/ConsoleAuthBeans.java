package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.RsaPasswordDecryptor;

/**
 * 控制台认证 Quarkus CDI 装配。
 */
@ApplicationScoped
public class ConsoleAuthBeans {

    @Produces
    @Singleton
    AuthConfig authConfig(NexusConsoleAuthConfig authProperties) {
        AuthConfig config = new AuthConfig();
        authProperties.tokenSecret().filter(secret -> !secret.isBlank()).ifPresent(config::setTokenSecret);
        config.setAccessTokenTtlSeconds(authProperties.accessTokenTtlSeconds());
        config.setRefreshTokenTtlSeconds(authProperties.refreshTokenTtlSeconds());
        config.setTenantLoginCaptchaEnabled(authProperties.tenantLoginCaptchaEnabled());
        config.setPlatformLoginCaptchaEnabled(authProperties.platformLoginCaptchaEnabled());
        return config;
    }

    @Produces
    @Singleton
    PasswordDecryptor passwordDecryptor(NexusConsoleAuthConfig authProperties) {
        String privateKey = authProperties.rsa().privateKey().orElse(null);
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException(
                    "nexus.console.auth.rsa.private-key is required for console authentication");
        }
        return new RsaPasswordDecryptor(privateKey);
    }
}
