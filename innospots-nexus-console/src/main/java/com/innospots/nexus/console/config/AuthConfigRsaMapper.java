package com.innospots.nexus.console.config;

/**
 * 将外部 RSA 配置映射为 {@link AuthConfig.Rsa}。
 */
public final class AuthConfigRsaMapper {

    private AuthConfigRsaMapper() {
    }

    /**
     * 映射公钥/私钥；未配置公钥且存在私钥时自动推导 SPKI 公钥。
     *
     * @param publicKeyBase64 可选 SPKI 公钥
     * @param privateKeyBase64 可选 PKCS#8 私钥
     * @return RSA 配置
     */
    public static AuthConfig.Rsa map(String publicKeyBase64, String privateKeyBase64) {
        AuthConfig.Rsa rsa = new AuthConfig.Rsa();
        String privateKey = trimToNull(privateKeyBase64);
        String publicKey = trimToNull(publicKeyBase64);
        if (publicKey == null && privateKey != null) {
            publicKey = RsaKeyMaterial.derivePublicKeyBase64(privateKey);
        }
        rsa.setPrivateKey(privateKey);
        rsa.setPublicKey(publicKey);
        return rsa;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
