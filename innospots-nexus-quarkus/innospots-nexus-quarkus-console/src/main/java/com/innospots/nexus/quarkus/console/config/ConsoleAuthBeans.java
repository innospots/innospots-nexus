package com.innospots.nexus.quarkus.console.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.auth.service.AuthRsaKeyService;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.config.AuthConfigRsaMapper;
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
        config.setLoginCaptchaEnabled(authProperties.loginCaptchaEnabled());
        config.setRsa(AuthConfigRsaMapper.map(
                authProperties.rsa().publicKey().orElse(null),
                authProperties.rsa().privateKey().orElse(null)));
        return config;
    }

    @Produces
    @Singleton
    PasswordDecryptor passwordDecryptor(AuthConfig authConfig) {
        String privateKey = authConfig.getRsa().getPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            throw new IllegalStateException(
                    "nexus.console.auth.rsa.private-key is required for console authentication");
        }
        return new RsaPasswordDecryptor(privateKey);
    }

    @Produces
    @Singleton
    AuthRsaKeyService authRsaKeyService(AuthConfig authConfig) {
        return new AuthRsaKeyService(authConfig);
    }
}
