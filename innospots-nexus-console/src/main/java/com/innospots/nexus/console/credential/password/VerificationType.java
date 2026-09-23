package com.innospots.nexus.console.credential.password;

/**
 * 忘记密码等流程中验证码的投递通道（与 {@link com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel} 在 EMAIL/MOBILE 上互转）。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum VerificationType {

    /**
     * 发送到用户邮箱的验证码。
     */
    EMAIL,

    /**
     * 发送到用户手机号的验证码。
     */
    MOBILE
}
