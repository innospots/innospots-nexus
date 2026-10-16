package com.innospots.nexus.console.config;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

import cn.hutool.core.codec.Base64;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * RSA 密钥材料解析：从 PKCS#8 私钥推导 SPKI 公钥（Base64）。
 */
public final class RsaKeyMaterial {

    private RsaKeyMaterial() {
    }

    /**
     * 从 Base64 PKCS#8 私钥推导 X.509 SPKI 公钥（Base64，无 PEM 头尾）。
     *
     * @param privateKeyBase64 PKCS#8 私钥
     * @return SPKI 公钥 Base64
     */
    public static String derivePublicKeyBase64(String privateKeyBase64) {
        if (privateKeyBase64 == null || privateKeyBase64.isBlank()) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, "RSA private key is required");
        }
        try {
            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKeyBase64.trim())));
            RSAPrivateCrtKey crt = (RSAPrivateCrtKey) privateKey;
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent()));
            return Base64.encode(publicKey.getEncoded());
        } catch (Exception ex) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, ex);
        }
    }
}
