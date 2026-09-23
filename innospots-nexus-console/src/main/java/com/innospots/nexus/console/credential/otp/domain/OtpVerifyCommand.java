package com.innospots.nexus.console.credential.otp.domain;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;

/**
 * 校验 OTP 的不可变命令；查找挑战时使用与发放相同的四维键：
 * realm + purpose + channel + 规范化 destination。
 *
 * @param securityRealm 与发放时相同的安全域
 * @param purpose       与发放时相同的 {@link OtpPurpose}
 * @param channel       与发放时相同的 {@link OtpChannel}
 * @param destination   与发放时相同的原始地址（将经相同规范化规则）
 * @param code          用户输入的数字验证码
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService#verifyOrThrow
 */
public record OtpVerifyCommand(
        SecurityRealm securityRealm,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination,
        String code
) {
}
