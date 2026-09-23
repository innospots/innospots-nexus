package com.innospots.nexus.spring.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.innospots.nexus.console.config.AuthConfig;

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

    /** 返回紧凑令牌密钥。 */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * 设置紧凑令牌密钥。
     *
     * @param tokenSecret 密钥；{@code null} 表示不覆盖默认
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    /** 返回访问令牌 TTL（秒）。 */
    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    /**
     * 设置访问令牌 TTL（秒）。
     *
     * @param accessTokenTtlSeconds 秒数
     */
    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    /** 返回刷新令牌 TTL（秒）。 */
    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }

    /**
     * 设置刷新令牌 TTL（秒）。
     *
     * @param refreshTokenTtlSeconds 秒数
     */
    public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    /** 返回租户域登录是否启用验证码。 */
    public boolean isTenantLoginCaptchaEnabled() {
        return tenantLoginCaptchaEnabled;
    }

    /**
     * 设置租户域登录是否启用验证码。
     *
     * @param tenantLoginCaptchaEnabled 是否启用
     */
    public void setTenantLoginCaptchaEnabled(boolean tenantLoginCaptchaEnabled) {
        this.tenantLoginCaptchaEnabled = tenantLoginCaptchaEnabled;
    }

    /** 返回平台域登录是否启用验证码。 */
    public boolean isPlatformLoginCaptchaEnabled() {
        return platformLoginCaptchaEnabled;
    }

    /**
     * 设置平台域登录是否启用验证码。
     *
     * @param platformLoginCaptchaEnabled 是否启用
     */
    public void setPlatformLoginCaptchaEnabled(boolean platformLoginCaptchaEnabled) {
        this.platformLoginCaptchaEnabled = platformLoginCaptchaEnabled;
    }

    /** 返回登录失败锁定阈值。 */
    public int getLoginMaxFailedAttempts() {
        return loginMaxFailedAttempts;
    }

    /**
     * 设置登录失败锁定阈值。
     *
     * @param loginMaxFailedAttempts 失败次数
     */
    public void setLoginMaxFailedAttempts(int loginMaxFailedAttempts) {
        this.loginMaxFailedAttempts = loginMaxFailedAttempts;
    }

    /** 返回登录锁定时长（分钟）。 */
    public int getLoginLockMinutes() {
        return loginLockMinutes;
    }

    /**
     * 设置登录锁定时长（分钟）。
     *
     * @param loginLockMinutes 分钟数
     */
    public void setLoginLockMinutes(int loginLockMinutes) {
        this.loginLockMinutes = loginLockMinutes;
    }

    /** 返回 RSA 解密配置。 */
    public Rsa getRsa() {
        return rsa;
    }

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
    public static class Rsa {

        /** PEM 或 Base64 编码的 RSA 私钥。 */
        private String privateKey;

        /** 返回 RSA 私钥。 */
        public String getPrivateKey() {
            return privateKey;
        }

        /**
         * 设置 RSA 私钥。
         *
         * @param privateKey 私钥材料
         */
        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }
    }
}
