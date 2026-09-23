package com.innospots.nexus.console.credential.totp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.innospots.nexus.base.util.Checks;

/**
 * 构建 {@code otpauth://} 扫码 URI，参数与 {@link com.innospots.nexus.console.credential.totp.algorithm.TotpCredentialAlgorithm} 一致（SHA1、8 位、30 秒）。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService
 */
public final class TotpProvisioningUriBuilder {

    private static final String DEFAULT_ISSUER = "Nexus";

    private TotpProvisioningUriBuilder() {
    }

    /**
     * 使用默认发行方名称「Nexus」构建 URI。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService#beginEnrollment} 返回给前端展示二维码。</p>
     *
     * @param accountLabel 账号展示名
     * @param base32Secret 共享密钥
     * @return {@code otpauth://totp/...} URI
     */
    public static String build(String accountLabel, String base32Secret) {
        return build(DEFAULT_ISSUER, accountLabel, base32Secret);
    }

    /**
     * 指定发行方构建 URI。
     *
     * @param issuer       authenticator 中显示的组织名
     * @param accountLabel 账号展示名
     * @param base32Secret 共享密钥
     * @return {@code otpauth://totp/...} URI
     */
    public static String build(String issuer, String accountLabel, String base32Secret) {
        Checks.notBlank(issuer, "issuer");
        Checks.notBlank(accountLabel, "accountLabel");
        Checks.notBlank(base32Secret, "base32Secret");
        String label = urlEncode(issuer + ":" + accountLabel);
        String secret = urlEncode(base32Secret);
        String issuerParam = urlEncode(issuer);
        return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + issuerParam + "&algorithm=SHA1&digits=8&period=30";
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
