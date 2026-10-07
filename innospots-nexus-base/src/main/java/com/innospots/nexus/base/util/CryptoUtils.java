package com.innospots.nexus.base.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

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
 * 密码学工具类：密码哈希（BCrypt）、对称加密（AES-GCM）与不对称加密（RSA/OAEP）。
 * RSA 操作支持大块数据的分块加密模式。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.exception.NexusException
 * @see com.innospots.nexus.base.status.NexusStatusCode
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

    /**
     * 计算给定字符串的 SHA-256 十六进制摘要。
     *
     * @param value 待摘要的字符串
     * @return SHA-256 十六进制字符串
     */
    public static String sha256Hex(String value) {
        return SecureUtil.sha256(value);
    }

    /**
     * 使用 BCrypt 加密原始密码。
     *
     * @param rawPassword 明文密码（不得为 null）
     * @return BCrypt 哈希字符串
     */
    public static String encryptPassword(String rawPassword) {
        Checks.notNull(rawPassword, "rawPassword");
        try {
            return BCrypt.hashpw(rawPassword);
        } catch (Exception e) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 生成用于密码哈希的 BCrypt 盐值。
     *
     * @return BCrypt 盐值字符串
     */
    public static String generatePasswordSalt() {
        try {
            return BCrypt.gensalt();
        } catch (Exception e) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 使用外部提供的盐值，以 BCrypt 加密原始密码。
     *
     * @param rawPassword 明文密码（不得为 null）
     * @param salt        外部提供的 BCrypt 盐值（不得为空白）
     * @return BCrypt 哈希字符串
     */
    public static String encryptPassword(String rawPassword, String salt) {
        Checks.notNull(rawPassword, "rawPassword");
        Checks.notBlank(salt, "salt");
        try {
            return BCrypt.hashpw(rawPassword, salt);
        } catch (Exception e) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 校验原始密码是否与 BCrypt 哈希匹配。
     *
     * @param rawPassword       明文密码
     * @param encryptedPassword BCrypt 哈希字符串
     * @return 匹配时返回 {@code true}
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
     * 使用 AES-GCM 及随机 12 字节 IV 加密明文。
     * IV 前置拼接在密文之前，结果经 Base64 编码。
     *
     * @param plaintext 待加密的明文
     * @param secret    AES 密钥字节
     * @return Base64 编码的 IV + 密文
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
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 解密由 {@link #encryptAesGcm} 产生的 AES-GCM 密文。
     * 期望前 12 字节为 IV。
     *
     * @param encrypted Base64 编码的 IV + 密文
     * @param secret    AES 密钥字节
     * @return 解密后的明文
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
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 生成默认 2048 位密钥长度的 RSA 密钥对。
     *
     * @return Base64 编码的公钥/私钥对
     */
    public static AsymmetricKeyPair generateRsaKeyPair() {
        return generateRsaKeyPair(DEFAULT_RSA_KEY_SIZE);
    }

    /**
     * 生成指定密钥长度的 RSA 密钥对。
     *
     * @param keySize 密钥长度（位），至少为 2048
     * @return Base64 编码的公钥/私钥对
     */
    public static AsymmetricKeyPair generateRsaKeyPair(int keySize) {
        Checks.isTrue(keySize >= DEFAULT_RSA_KEY_SIZE, "RSA key size must be at least 2048 bits");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            generator.initialize(keySize);
            KeyPair keyPair = generator.generateKeyPair();
            return new AsymmetricKeyPair(
                    Base64.encode(keyPair.getPublic().getEncoded()),
                    Base64.encode(keyPair.getPrivate().getEncoded())
            );
        } catch (Exception e) {
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 使用 RSA-OAEP（SHA-256）加密明文。
     * 对超过密钥模数长度的数据支持分块加密模式。
     *
     * @param plaintext 待加密的明文
     * @param publicKey Base64 编码的 X.509 公钥
     * @return Base64 编码的密文
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
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
        }
    }

    /**
     * 使用对应私钥解密 RSA 密文。
     *
     * @param encrypted  Base64 编码的密文
     * @param privateKey Base64 编码的 PKCS#8 私钥
     * @return 解密后的明文
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
            throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, e);
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
        throw NexusException.build(NexusStatusCode.CRYPTO_FAILED, "Key is not an RSA key");
    }

    private static byte[] cipherBlocks(Cipher cipher, byte[] input, int blockSize) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int offset = 0; offset < input.length; offset += blockSize) {
            int length = Math.min(blockSize, input.length - offset);
            output.write(cipher.doFinal(input, offset, length));
        }
        return output.toByteArray();
    }

    /**
     * RSA 不对称密钥对，公钥与私钥均为 Base64 编码字符串。
     *
     * @author Smars
     * @date 2026/09/13
     * @param publicKey  Base64 编码的公钥
     * @param privateKey Base64 编码的私钥
     */
    public record AsymmetricKeyPair(String publicKey, String privateKey) {
    }
}
