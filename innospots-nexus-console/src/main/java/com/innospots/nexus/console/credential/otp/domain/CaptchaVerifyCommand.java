package com.innospots.nexus.console.credential.otp.domain;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;

/**
 * 校验用户提交的图形验证码。
 *
 * @param securityRealm 与发放一致的安全域
 * @param purpose       与发放一致的用途
 * @param clientKey     与发放一致的客户端键
 * @param captchaCode   用户输入的图形验证码
 */
public record CaptchaVerifyCommand(
        SecurityRealm securityRealm,
        OtpPurpose purpose,
        String clientKey,
        String captchaCode
) {
}
