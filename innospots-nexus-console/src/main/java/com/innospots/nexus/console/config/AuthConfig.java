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
     * 登录是否要求图形验证码（本进程仅服务单一安全域，不区分 platform/tenant）。
     */
    private boolean loginCaptchaEnabled = true;

    /**
     * 登录密码 RSA 加解密密钥（{@code nexus.console.auth.rsa.*}）。
     */
    private Rsa rsa = new Rsa();

    /**
     * 设置 RSA 配置。
     *
     * @param rsa RSA 配置；{@code null} 时重置为默认空实例
     */
    public void setRsa(Rsa rsa) {
        this.rsa = rsa == null ? new Rsa() : rsa;
    }

    /**
     * {@code nexus.console.auth.rsa.*}：前端加密公钥与后端解密私钥。
     */
    @Getter
    @Setter
    public static class Rsa {

        /** PEM 或 Base64 编码的 X.509 SPKI 公钥。 */
        private String publicKey;

        /** PEM 或 Base64 编码的 PKCS#8 私钥。 */
        private String privateKey;
    }
}
