package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.RsaPasswordDecryptor;

/**
 * 控制台认证 Spring 装配：将 {@link ConsoleAuthProperties} 绑定为 {@link AuthConfig} 等 Bean。
 *
 * @author Smars
 * @date 2026/09/23
 * @see ConsoleAuthProperties
 * @see AuthConfig
 */
@Configuration
@EnableConfigurationProperties(ConsoleAuthProperties.class)
public class ConsoleAuthConfiguration {

    /** 将 Spring 属性映射为 framework-neutral {@link AuthConfig}。 */
    @Bean
    AuthConfig authConfig(ConsoleAuthProperties authProperties) {
        return toAuthConfig(authProperties);
    }

    /** 配置 RSA 私钥时启用登录密码解密。 */
    @Bean
    @ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
    PasswordDecryptor passwordDecryptor(ConsoleAuthProperties authProperties) {
        return new RsaPasswordDecryptor(authProperties.getRsa().getPrivateKey());
    }

    private static AuthConfig toAuthConfig(ConsoleAuthProperties properties) {
        AuthConfig config = new AuthConfig();
        String tokenSecret = properties.getTokenSecret();
        if (tokenSecret != null && !tokenSecret.isBlank()) {
            config.setTokenSecret(tokenSecret);
        }
        config.setAccessTokenTtlSeconds(properties.getAccessTokenTtlSeconds());
        config.setRefreshTokenTtlSeconds(properties.getRefreshTokenTtlSeconds());
        config.setTenantLoginCaptchaEnabled(properties.isTenantLoginCaptchaEnabled());
        config.setPlatformLoginCaptchaEnabled(properties.isPlatformLoginCaptchaEnabled());
        return config;
    }
}
