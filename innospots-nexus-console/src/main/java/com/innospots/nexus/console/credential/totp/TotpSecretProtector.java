package com.innospots.nexus.console.credential.totp;

import java.security.SecureRandom;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.base.util.CryptoUtils;

/**
 * TOTP 共享密钥的 Base32 生成与 AES-GCM 静态加密；主密钥由 {@link TotpMasterKeyProvider} 提供。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.base.util.CryptoUtils#encryptAesGcm(String, String)
 */
public final class TotpSecretProtector {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SECRET_BYTES = 20;
    private static final char[] BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private final TotpMasterKeyProvider masterKeyProvider;

    /**
     * @param masterKeyProvider 应用级 AES-GCM 主密钥来源
     */
    public TotpSecretProtector(TotpMasterKeyProvider masterKeyProvider) {
        this.masterKeyProvider = Checks.notNull(masterKeyProvider, "masterKeyProvider");
    }

    /**
     * 生成新的 Base32 共享密钥（RFC 4648，无填充）。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.totp.service.DefaultTotpEnrollmentService#beginEnrollment}。</p>
     *
     * @return 供 authenticator 与 {@link #encryptForStorage} 使用的 Base32 字符串
     */
    public String generateBase32Secret() {
        byte[] bytes = new byte[SECRET_BYTES];
        RANDOM.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * 将明文共享密钥加密后写入 {@code verifier} 列。
     * <p>调用场景：TOTP 注册开始时持久化密钥，库中永不存明文。</p>
     *
     * @param base32Secret 注册阶段生成的共享密钥
     * @return AES-GCM 密文字符串
     */
    public String encryptForStorage(String base32Secret) {
        Checks.notBlank(base32Secret, "base32Secret");
        return CryptoUtils.encryptAesGcm(base32Secret, masterKeyProvider.masterKey());
    }

    /**
     * 从库中 verifier 解密得到 Base32 共享密钥。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.totp.service.TotpVerificationSupport#verifyCode}。</p>
     *
     * @param encryptedVerifier 持久化的 AES-GCM 载荷
     * @return Base32 共享密钥
     */
    public String decryptFromStorage(String encryptedVerifier) {
        Checks.notBlank(encryptedVerifier, "encryptedVerifier");
        return CryptoUtils.decryptAesGcm(encryptedVerifier, masterKeyProvider.masterKey());
    }

    /**
     * 将 Base32 解码为 HMAC 所需的原始密钥字节。
     *
     * @param base32Secret RFC 4648 Base32 字符串
     * @return 解码后的密钥字节
     */
    public byte[] decodeBase32Secret(String base32Secret) {
        Checks.notBlank(base32Secret, "base32Secret");
        return decodeBase32(base32Secret);
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte value : data) {
            buffer = (buffer << 8) | (value & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                result.append(BASE32_ALPHABET[index]);
            }
        }
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            result.append(BASE32_ALPHABET[index]);
        }
        return result.toString();
    }

    private static byte[] decodeBase32(String encoded) {
        String normalized = encoded.trim().replace(" ", "").toUpperCase();
        byte[] output = new byte[normalized.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            int value = base32Value(ch);
            if (value < 0) {
                throw new IllegalArgumentException("Invalid Base32 character: " + ch);
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        if (index == output.length) {
            return output;
        }
        byte[] trimmed = new byte[index];
        System.arraycopy(output, 0, trimmed, 0, index);
        return trimmed;
    }

    private static int base32Value(char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return ch - 'A';
        }
        if (ch >= '2' && ch <= '7') {
            return ch - '2' + 26;
        }
        return -1;
    }
}
