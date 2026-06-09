package com.innospots.nexus.kernel.user.api;

/**
 * Decrypts password ciphertext submitted by frontend registration clients.
 * <p>Implementations hide the concrete frontend encryption method so user
 * registration can evolve from RSA to other password transport schemes without
 * changing request objects or persistence operators.</p>
 */
public interface UserPasswordDecryptor {

    /**
     * Decrypts a frontend encrypted password into a raw password string.
     *
     * @param encryptedPassword frontend encrypted password payload
     * @return raw password string for server-side password hashing
     */
    String decrypt(String encryptedPassword);
}
