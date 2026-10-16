package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.auth.service.AuthRsaKeyService;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.config.AuthConfigRsaMapper;
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
    PasswordDecryptor passwordDecryptor(AuthConfig authConfig) {
        String privateKey = authConfig.getRsa().getPrivateKey();
        return new RsaPasswordDecryptor(privateKey);
    }

    @Bean
    AuthRsaKeyService authRsaKeyService(AuthConfig authConfig) {
        return new AuthRsaKeyService(authConfig);
    }

    private static AuthConfig toAuthConfig(ConsoleAuthProperties properties) {
        AuthConfig config = new AuthConfig();
        String tokenSecret = properties.getTokenSecret();
        if (tokenSecret != null && !tokenSecret.isBlank()) {
            config.setTokenSecret(tokenSecret);
        }
        config.setAccessTokenTtlSeconds(properties.getAccessTokenTtlSeconds());
        config.setRefreshTokenTtlSeconds(properties.getRefreshTokenTtlSeconds());
        config.setLoginCaptchaEnabled(properties.isLoginCaptchaEnabled());
        ConsoleAuthProperties.Rsa rsaProperties = properties.getRsa();
        if (rsaProperties == null) {
            config.setRsa(new AuthConfig.Rsa());
        } else {
            config.setRsa(AuthConfigRsaMapper.map(
                    rsaProperties.getPublicKey(), rsaProperties.getPrivateKey()));
        }
        return config;
    }
}
