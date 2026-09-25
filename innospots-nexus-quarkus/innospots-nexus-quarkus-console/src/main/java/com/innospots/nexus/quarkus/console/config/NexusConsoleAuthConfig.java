package com.innospots.nexus.quarkus.console.config;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * 绑定 {@code nexus.console.auth.*}（对齐 Spring {@code ConsoleAuthProperties}）。
 */
@ConfigMapping(prefix = "nexus.console.auth")
public interface NexusConsoleAuthConfig {

    Optional<String> tokenSecret();

    @WithDefault("7200")
    long accessTokenTtlSeconds();

    @WithDefault("604800")
    long refreshTokenTtlSeconds();

    @WithDefault("true")
    boolean tenantLoginCaptchaEnabled();

    @WithDefault("true")
    boolean platformLoginCaptchaEnabled();

    @WithDefault("5")
    int loginMaxFailedAttempts();

    @WithDefault("15")
    int loginLockMinutes();

    Rsa rsa();

    /**
     * {@code nexus.console.auth.rsa.*}。
     */
    interface Rsa {

        @WithName("private-key")
        Optional<String> privateKey();
    }
}
