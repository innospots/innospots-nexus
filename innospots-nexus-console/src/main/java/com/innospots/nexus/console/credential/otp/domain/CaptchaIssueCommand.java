package com.innospots.nexus.console.credential.otp.domain;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.captcha.CaptchaPolicy;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;

/**
 * 发放图形验证码。
 *
 * @param securityRealm 安全域
 * @param purpose       业务用途（建议 {@link OtpPurpose#CAPTCHA}）
 * @param clientKey     客户端会话键（浏览器 tab、登录事务 ID 等），作为挑战表的 destination
 * @param captchaPolicy 绘制参数，null 时使用 {@link CaptchaPolicy#DEFAULT}
 */
public record CaptchaIssueCommand(
        SecurityRealm securityRealm,
        OtpPurpose purpose,
        String clientKey,
        CaptchaPolicy captchaPolicy
) {
}
