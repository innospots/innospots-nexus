package com.innospots.nexus.kernel.user.api;

import java.util.Objects;

import com.innospots.nexus.base.util.CryptoUtils;

/**
 * RSA implementation for frontend encrypted user registration passwords.
 *
 * @param privateKey Base64-encoded PKCS#8 private key
 */
public record RsaUserPasswordDecryptor(String privateKey) implements UserPasswordDecryptor {

    public RsaUserPasswordDecryptor {
        Objects.requireNonNull(privateKey, "privateKey must not be null");
    }

    @Override
    public String decrypt(String encryptedPassword) {
        return CryptoUtils.decryptRsa(encryptedPassword, privateKey);
    }
}
