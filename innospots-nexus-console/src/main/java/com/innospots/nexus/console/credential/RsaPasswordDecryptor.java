package com.innospots.nexus.console.credential;

import java.util.Objects;

import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.credential.api.PasswordDecryptor;

/**
 * RSA implementation for frontend encrypted passwords.
 *
 * @param privateKey Base64-encoded PKCS#8 private key
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
