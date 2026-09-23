package com.innospots.nexus.console.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 控制台认证的令牌签发设置。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
public class AuthConfig {

    /**
     * 测试与本地开发使用的默认 AES 密钥。
     */
    public static final String DEFAULT_TOKEN_SECRET = "0123456789abcdef";

    /**
     * 紧凑令牌 AES 加密密钥。
     */
    private String tokenSecret = DEFAULT_TOKEN_SECRET;

    /**
     * 访问令牌有效期（秒）。
     */
    private long accessTokenTtlSeconds = 7200L;

    /**
     * 刷新令牌有效期（秒）。
     */
    private long refreshTokenTtlSeconds = 604800L;

    /**
     * 租户域登录是否要求图形验证码。
     */
    private boolean tenantLoginCaptchaEnabled = true;

    /**
     * 平台域登录是否要求图形验证码。
     */
    private boolean platformLoginCaptchaEnabled = true;
}
