package com.innospots.nexus.console.credential.otp.policy;

import java.time.Duration;

/**
 * OTP 发放与校验的可配置策略（不可变 record）。
 * <p>生产默认见 {@link #DEFAULT}；单测可构造更短 TTL/零冷却以加速用例。</p>
 *
 * @param codeLength        数字验证码位数（4–10）
 * @param ttl               自创建起挑战有效时长
 * @param resendCooldown    同一投递键两次 {@code issue} 的最小间隔
 * @param maxVerifyAttempts 单挑战允许连续错误校验次数，用尽后挑战作废
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService
 */
public record OtpPolicy(
        int codeLength,
        Duration ttl,
        Duration resendCooldown,
        int maxVerifyAttempts
) {

    /**
     * 控制台推荐基线：6 位码、10 分钟有效、60 秒重发冷却、最多 5 次错误尝试。
     */
    public static final OtpPolicy DEFAULT = new OtpPolicy(
            6,
            Duration.ofMinutes(10),
            Duration.ofSeconds(60),
            5);

    /**
     * 紧凑构造器：非法参数立即失败，避免无效策略进入运行时。
     */
    public OtpPolicy {
        if (codeLength < 4 || codeLength > 10) {
            throw new IllegalArgumentException("codeLength must be between 4 and 10");
        }
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
        if (resendCooldown.isNegative()) {
            throw new IllegalArgumentException("resendCooldown must not be negative");
        }
        if (maxVerifyAttempts < 1) {
            throw new IllegalArgumentException("maxVerifyAttempts must be at least 1");
        }
    }
}
