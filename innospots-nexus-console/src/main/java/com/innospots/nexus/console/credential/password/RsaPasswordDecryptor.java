package com.innospots.nexus.console.credential.password;

import java.util.Objects;

import com.innospots.nexus.base.util.CryptoUtils;

/**
 * 前端 RSA 加密密码的 {@link PasswordDecryptor} 实现。
 *
 * @author Smars
 * @date 2026/09/13
 * @param privateKey Base64 编码的 PKCS#8 私钥
 */
public record RsaPasswordDecryptor(String privateKey) implements PasswordDecryptor {

    public RsaPasswordDecryptor {
        Objects.requireNonNull(privateKey, "privateKey must not be null");
    }

    @Override
    public String decrypt(String encryptedPassword) {
        return CryptoUtils.decryptRsa(encryptedPassword, privateKey);
    }
}
