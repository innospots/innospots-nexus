package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * TOTP 凭据加密的 Spring 配置属性。
 *
 * <p>绑定 {@code nexus.console.credential.totp.*}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see ConsoleCredentialConfiguration
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus.console.credential.totp")
public class ConsoleCredentialTotpProperties {

    /** TOTP 密钥加密主密钥。 */
    private String masterKey;
}
