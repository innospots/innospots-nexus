package com.innospots.nexus.console.credential.otp.domain.enums;

/**
 * OTP 业务用途；持久化为 {@link com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity#getPurpose()} 字符串。
 * <p>校验时必须与发放时一致，防止用「忘记密码」码完成「登录 step-up」等跨场景复用。</p>
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService
 */
public enum OtpPurpose {

    /**
     * 忘记密码 / 重置密码。
     */
    PASSWORD_RESET,

    /**
     * 登录二次校验（短信/邮件 OTP）。
     */
    LOGIN_STEP_UP,

    /**
     * 绑定或变更联系方式。
     */
    BIND_CONTACT,

    /**
     * 图形人机校验（登录前、发送短信 OTP 前等）。
     */
    CAPTCHA
}
