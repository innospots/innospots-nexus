package com.innospots.nexus.console.credential.totp.algorithm;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.innospots.nexus.base.util.Checks;

/**
 * RFC 6238 TOTP（HMAC-SHA1，30 秒步长，8 位数字）；校验允许 ±1 个时间窗。
 *
 * @author Smars
 * @date 2026/09/19
 * @see TotpCredentialAlgorithms
 */
public final class TotpCredentialAlgorithm {

    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 8;

    /**
     * 持久化到凭据行的算法标识。
     *
     * @return {@link TotpCredentialAlgorithms#RFC6238_SHA1_V1}
     */
    public String algorithmId() {
        return TotpCredentialAlgorithms.RFC6238_SHA1_V1;
    }

    /**
     * 在指定时刻生成 TOTP（测试与向量校验用）。
     * <p>调用场景：单元测试断言 RFC 6238 向量，非生产登录路径。</p>
     *
     * @param secret  HMAC 密钥字节
     * @param instant 时间锚点
     * @return 8 位数字动态码字符串
     */
    public String generateAt(byte[] secret, Instant instant) {
        Checks.notNull(secret, "secret");
        Checks.notNull(instant, "instant");
        long counter = instant.getEpochSecond() / TIME_STEP_SECONDS;
        return formatCode(hotp(secret, counter));
    }

    /**
     * 校验动态码，允许前后各一个时间窗以容忍时钟漂移。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.totp.service.TotpVerificationSupport#verifyCode}。</p>
     *
     * @param secret  HMAC 密钥字节
     * @param code    用户输入
     * @param instant 校验时刻（通常为 {@link Instant#now()})
     * @return 任一时间窗匹配时为 {@code true}
     */
    public boolean verify(byte[] secret, String code, Instant instant) {
        Checks.notBlank(code, "code");
        Checks.notNull(secret, "secret");
        Checks.notNull(instant, "instant");
        long counter = instant.getEpochSecond() / TIME_STEP_SECONDS;
        for (long offset = -1; offset <= 1; offset++) {
            if (code.equals(formatCode(hotp(secret, counter + offset)))) {
                return true;
            }
        }
        return false;
    }

    private static int hotp(byte[] secret, long counter) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            int modulus = (int) Math.pow(10, CODE_DIGITS);
            return binary % modulus;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("TOTP HMAC failed", ex);
        }
    }

    private static String formatCode(int value) {
        return String.format("%0" + CODE_DIGITS + "d", value);
    }
}
