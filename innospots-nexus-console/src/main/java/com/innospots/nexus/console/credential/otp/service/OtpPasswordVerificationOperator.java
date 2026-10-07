package com.innospots.nexus.console.credential.otp.service;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.VerificationType;

/**
 * 将 {@link PasswordVerificationOperator} 映射到 OTP 子域，且用途固定为
 * {@link com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose#PASSWORD_RESET}。
 * <p>portal 用户改密流程只依赖该端口，无需直接引用 {@link OtpChallengeService}。</p>
 *
 * @author Smars
 * @date 2026/09/19
 * @see PasswordVerificationOperator
 * @see OtpChallengeService
 */
public final class OtpPasswordVerificationOperator implements PasswordVerificationOperator {

    private final OtpChallengeService otpChallengeService;
    private final SecurityRealm securityRealm;

    /**
     * 租户域下的忘记密码 OTP（默认 {@link SecurityRealm#TENANT}）。
     *
     * @param otpChallengeService OTP 挑战服务
     */
    public OtpPasswordVerificationOperator(OtpChallengeService otpChallengeService) {
        this(otpChallengeService, SecurityRealm.TENANT);
    }

    /**
     * @param otpChallengeService OTP 挑战服务
     * @param securityRealm       平台或租户安全域
     */
    public OtpPasswordVerificationOperator(OtpChallengeService otpChallengeService, SecurityRealm securityRealm) {
        this.otpChallengeService = Checks.notNull(otpChallengeService, "otpChallengeService");
        this.securityRealm = Checks.notNull(securityRealm, "securityRealm");
    }

    /**
     * {@inheritDoc}
     * <p>调用场景：用户请求「发送重置密码验证码」。</p>
     */
    @Override
    public void sendVerificationCode(String identity, VerificationType type) {
        otpChallengeService.sendPasswordResetCode(securityRealm, identity, type, null);
    }

    /**
     * {@inheritDoc}
     * <p>调用场景：提交新密码前校验邮箱/短信验证码。</p>
     */
    @Override
    public boolean verifyVerificationCode(String identity, VerificationType type, String code) {
        return otpChallengeService.verifyPasswordResetCode(securityRealm, identity, type, code);
    }

    /**
     * {@inheritDoc}
     * <p>调用场景：新密码写入成功后作废 OTP，防止重复改密。</p>
     */
    @Override
    public void expireVerificationCode(String identity, VerificationType type) {
        otpChallengeService.expirePasswordResetCode(securityRealm, identity, type);
    }
}
