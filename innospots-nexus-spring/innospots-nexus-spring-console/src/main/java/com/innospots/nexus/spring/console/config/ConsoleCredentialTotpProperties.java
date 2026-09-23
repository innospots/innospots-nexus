package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TOTP 凭据加密的 Spring 配置属性。
 *
 * <p>绑定 {@code nexus.console.credential.totp.*}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see ConsoleCredentialConfiguration
 */
@ConfigurationProperties(prefix = "nexus.console.credential.totp")
public class ConsoleCredentialTotpProperties {

    /** TOTP 密钥加密主密钥。 */
    private String masterKey;

    /** 返回 TOTP 主密钥。 */
    public String getMasterKey() {
        return masterKey;
    }

    /**
     * 设置 TOTP 主密钥。
     *
     * @param masterKey 主密钥材料
     */
    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }
}
