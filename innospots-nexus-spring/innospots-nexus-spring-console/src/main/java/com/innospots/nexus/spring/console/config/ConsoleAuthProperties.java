package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.console.config.AuthConfig;

import lombok.Getter;
import lombok.Setter;

/**
 * 控制台认证相关的 Spring 配置属性。
 *
 * <p>绑定 {@code nexus.console.auth.*}；由 {@link ConsoleAuthConfiguration} 转为 {@link AuthConfig}。</p>
 *
 * @author Smars
 * @date 2026/09/23
 * @see AuthConfig
 * @see ConsoleAuthConfiguration
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nexus.console.auth")
public class ConsoleAuthProperties {

    /** 紧凑令牌 AES 密钥；未配置时使用 {@link AuthConfig#DEFAULT_TOKEN_SECRET}。 */
    private String tokenSecret;

    /** 访问令牌有效期（秒）。 */
    private long accessTokenTtlSeconds = 7200L;

    /** 刷新令牌有效期（秒）。 */
    private long refreshTokenTtlSeconds = 604_800L;

    /** 租户域登录是否要求图形验证码。 */
    private boolean tenantLoginCaptchaEnabled = true;

    /** 平台域登录是否要求图形验证码。 */
    private boolean platformLoginCaptchaEnabled = true;

    /** 连续登录失败多少次后锁定账号。 */
    private int loginMaxFailedAttempts = 5;

    /** 登录锁定时长（分钟）。 */
    private int loginLockMinutes = 15;

    /** {@code nexus.console.auth.rsa.*}。 */
    private Rsa rsa = new Rsa();

    /**
     * 设置 RSA 解密配置。
     *
     * @param rsa RSA 配置；{@code null} 时重置为默认空实例
     */
    public void setRsa(Rsa rsa) {
        this.rsa = rsa == null ? new Rsa() : rsa;
    }

    /**
     * {@code nexus.console.auth.rsa.*} 登录密码 RSA 解密。
     *
     * <p>映射示例：{@code nexus.console.auth.rsa.private-key}。</p>
     *
     * @author Smars
     * @date 2026/09/23
     */
    @Getter
    @Setter
    public static class Rsa {

        /** PEM 或 Base64 编码的 RSA 私钥。 */
        private String privateKey;
    }
}
