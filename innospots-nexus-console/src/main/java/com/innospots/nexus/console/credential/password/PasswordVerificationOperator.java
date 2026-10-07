package com.innospots.nexus.console.credential.password;

/**
 * 忘记密码等场景的验证码端口；OTP 实现见 {@link com.innospots.nexus.console.credential.otp.service.OtpPasswordVerificationOperator}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.portal.user.operator.PasswordOperator
 */
public interface PasswordVerificationOperator {

    void sendVerificationCode(String identity, VerificationType type);

    boolean verifyVerificationCode(String identity, VerificationType type, String code);

    void expireVerificationCode(String identity, VerificationType type);
}
