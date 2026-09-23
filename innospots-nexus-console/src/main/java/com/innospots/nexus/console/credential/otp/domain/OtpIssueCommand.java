package com.innospots.nexus.console.credential.otp.domain;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;

/**
 * 发放 OTP 的不可变命令对象；由 API 层组装后交给 {@link com.innospots.nexus.console.credential.otp.service.OtpChallengeService#issue}。
 *
 * @param securityRealm 决定 {@link com.innospots.nexus.console.scope.ConsoleOwnership} 归属列
 * @param purpose       与校验、模板键绑定的业务场景，不可与校验时混用
 * @param channel       下发通道，决定地址规范化与事件类型
 * @param destination   用户输入的邮箱、手机号等（服务内调用 {@link OtpChannel#normalizeDestination})
 * @param locale        可选的模板语言（如 {@code zh}、{@code en}）
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService#issue
 */
public record OtpIssueCommand(
        SecurityRealm securityRealm,
        OtpPurpose purpose,
        OtpChannel channel,
        String destination,
        String locale
) {
}
