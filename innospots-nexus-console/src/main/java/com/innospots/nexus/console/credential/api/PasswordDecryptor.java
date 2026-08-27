package com.innospots.nexus.console.credential.api;

/**
 * Decrypts password ciphertext submitted by frontend clients.
 */
public interface PasswordDecryptor {

    /**
     * Decrypts a frontend encrypted password into a raw password string.
     *
     * @param encryptedPassword frontend encrypted password payload
     * @return raw password string for server-side hashing
     */
    String decrypt(String encryptedPassword);
}
