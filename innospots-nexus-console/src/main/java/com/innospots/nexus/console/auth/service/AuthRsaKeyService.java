package com.innospots.nexus.console.auth.service;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.vo.AuthRsaPublicKeyVo;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.config.RsaKeyMaterial;

/**
 * 登录密码 RSA 公钥查询（供前端加密使用）。
 */
public class AuthRsaKeyService {

    private final AuthConfig authConfig;

    public AuthRsaKeyService(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    /**
     * 返回配置的 SPKI 公钥；未显式配置时从私钥推导。
     *
     * @return 公钥载荷
     */
    public AuthRsaPublicKeyVo getPasswordEncryptionPublicKey() {
        String publicKeyBase64 = resolvePublicKeyBase64();
        if (publicKeyBase64 == null || publicKeyBase64.isBlank()) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, "RSA public key is not configured");
        }
        return new AuthRsaPublicKeyVo(publicKeyBase64);
    }

    private String resolvePublicKeyBase64() {
        AuthConfig.Rsa rsa = authConfig.getRsa();
        if (rsa == null) {
            return null;
        }
        String configured = rsa.getPublicKey();
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        String privateKey = rsa.getPrivateKey();
        if (privateKey == null || privateKey.isBlank()) {
            return null;
        }
        return RsaKeyMaterial.derivePublicKeyBase64(privateKey);
    }
}
