package com.innospots.nexus.base.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.innospots.nexus.base.exception.NexusException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Cryptographic utilities: password hashing (BCrypt), symmetric encryption
 * (AES-GCM), and asymmetric encryption (RSA/OAEP). RSA operations support
 * block-mode encryption for large payloads.
 */
public final class CryptoUtils {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int DEFAULT_RSA_KEY_SIZE = 2048;
    private static final int SHA256_OAEP_PADDING_BYTES = 66;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private CryptoUtils() {
    }

    /** Computes SHA-256 hex digest of the given string. */
    public static String sha256Hex(String value) {
        return SecureUtil.sha256(value);
    }

    /**
     * Encrypts a raw password using BCrypt.
     *
     * @param rawPassword the plain-text password (must not be null)
     * @return the BCrypt hash string
     */
    public static String encryptPassword(String rawPassword) {
        if (rawPassword == null) {
            throw new NexusException("PASSWORD_ENCRYPT_FAILED", "Raw password must not be null");
        }
        try {
            return BCrypt.hashpw(rawPassword);
        } catch (Exception e) {
            throw new NexusException("PASSWORD_ENCRYPT_FAILED", "Failed to encrypt password", e);
        }
    }

    /**
     * Verifies a raw password against a BCrypt hash.
     *
     * @return true if the password matches the hash
     */
    public static boolean matchesPassword(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null || encryptedPassword.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encryptedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Encrypts plaintext using AES-GCM with a random 12-byte IV.
     * The IV is prepended to the ciphertext; the result is Base64-encoded.
     *
     * @param plaintext the text to encrypt
     * @param secret    the AES secret key bytes
     * @return Base64-encoded IV + ciphertext
     */
    public static String encryptAesGcm(String plaintext, String secret) {
        try {
            byte[] iv = RandomUtil.randomBytes(GCM_IV_BYTES);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(secret), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.encode(payload);
        } catch (Exception e) {
            throw new NexusException("AES_GCM_ENCRYPT_FAILED", "Failed to encrypt text", e);
        }
    }

    /**
     * Decrypts AES-GCM ciphertext produced by {@link #encryptAesGcm}.
     * Expects the first 12 bytes to be the IV.
     *
     * @param encrypted Base64-encoded IV + ciphertext
     * @param secret    the AES secret key bytes
     * @return the decrypted plaintext
     */
    public static String decryptAesGcm(String encrypted, String secret) {
        try {
            byte[] payload = Base64.decode(encrypted);
            byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_BYTES);
            byte[] cipherText = Arrays.copyOfRange(payload, GCM_IV_BYTES, payload.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(secret), new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new NexusException("AES_GCM_DECRYPT_FAILED", "Failed to decrypt text", e);
        }
    }

    /** Generates an RSA key pair with the default 2048-bit key size. */
    public static AsymmetricKeyPair generateRsaKeyPair() {
        return generateRsaKeyPair(DEFAULT_RSA_KEY_SIZE);
    }

    /**
     * Generates an RSA key pair with the specified key size.
     *
     * @param keySize must be at least 2048
     * @return the Base64-encoded public/private key pair
     */
    public static AsymmetricKeyPair generateRsaKeyPair(int keySize) {
        if (keySize < DEFAULT_RSA_KEY_SIZE) {
            throw new NexusException("RSA_KEY_SIZE_TOO_SMALL", "RSA key size must be at least 2048 bits");
        }
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            return new AsymmetricKeyPair(
                    Base64.encode(keyPair.getPublic().getEncoded()),
                    Base64.encode(keyPair.getPrivate().getEncoded())
            );
        } catch (Exception e) {
            throw new NexusException("RSA_KEY_PAIR_GENERATE_FAILED", "Failed to generate RSA key pair", e);
        }
    }

    /**
     * Encrypts plaintext using RSA-OAEP with SHA-256.
     * Supports block-mode encryption for data larger than the key modulus.
     *
     * @param plaintext the text to encrypt
     * @param publicKey Base64-encoded X.509 public key
     * @return Base64-encoded ciphertext
     */
    public static String encryptRsa(String plaintext, String publicKey) {
        try {
            PublicKey key = publicKey(publicKey);
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            int blockSize = rsaKeyBytes(key) - SHA256_OAEP_PADDING_BYTES;
            byte[] encrypted = cipherBlocks(cipher, plaintext.getBytes(StandardCharsets.UTF_8), blockSize);
            return Base64.encode(encrypted);
        } catch (Exception e) {
            throw new NexusException("RSA_ENCRYPT_FAILED", "Failed to encrypt text with RSA public key", e);
        }
    }

    /**
     * Decrypts RSA ciphertext using the corresponding private key.
     *
     * @param encrypted  Base64-encoded ciphertext
     * @param privateKey Base64-encoded PKCS#8 private key
     * @return decrypted plaintext
     */
    public static String decryptRsa(String encrypted, String privateKey) {
        try {
            PrivateKey key = privateKey(privateKey);
            Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherText = Base64.decode(encrypted);
            byte[] plaintext = cipherBlocks(cipher, cipherText, rsaKeyBytes(key));
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new NexusException("RSA_DECRYPT_FAILED", "Failed to decrypt text with RSA private key", e);
        }
    }

    private static SecretKeySpec key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
    }

    private static PublicKey publicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(new X509EncodedKeySpec(Base64.decode(publicKey)));
    }

    private static PrivateKey privateKey(String privateKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(privateKey)));
    }

    private static int rsaKeyBytes(java.security.Key key) {
        if (key instanceof RSAKey rsaKey) {
            return (rsaKey.getModulus().bitLength() + 7) / 8;
        }
        throw new NexusException("RSA_KEY_INVALID", "Key is not an RSA key");
    }

    private static byte[] cipherBlocks(Cipher cipher, byte[] input, int blockSize) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int offset = 0; offset < input.length; offset += blockSize) {
            int length = Math.min(blockSize, input.length - offset);
            output.write(cipher.doFinal(input, offset, length));
        }
        return output.toByteArray();
    }

    public record AsymmetricKeyPair(String publicKey, String privateKey) {
    }
}
