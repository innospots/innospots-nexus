package com.innospots.nexus.console.credential.otp.service;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.credential.otp.captcha.CaptchaPolicy;
import com.innospots.nexus.console.credential.otp.captcha.CaptchaStyle;
import com.innospots.nexus.console.credential.otp.captcha.HutoolCaptchaFactory;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueResult;
import com.innospots.nexus.console.credential.otp.domain.CaptchaVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;

/**
 * 图形验证码（{@code captchaCode}）发放与校验门面，底层复用 {@link OtpChallengeService} 挑战表。
 * <p>图片由 Hutool 同步生成，不发布 {@link com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent}。</p>
 */
public final class CaptchaChallengeService {

    private final OtpChallengeService otpChallengeService;

    public CaptchaChallengeService(OtpChallengeService otpChallengeService) {
        this.otpChallengeService = Checks.notNull(otpChallengeService, "otpChallengeService");
    }

    /**
     * 生成图形验证码并持久化哈希，返回 Base64 图片。
     *
     * @param command 发放命令
     * @return 挑战 ID、图片与过期时间
     */
    public CaptchaIssueResult issue(CaptchaIssueCommand command) {
        Checks.notNull(command, "command");
        Checks.notNull(command.securityRealm(), "securityRealm");
        Checks.notNull(command.purpose(), "purpose");
        Checks.notBlank(command.clientKey(), "clientKey");
        CaptchaPolicy captchaPolicy = command.captchaPolicy() == null ? CaptchaPolicy.DEFAULT : command.captchaPolicy();
        HutoolCaptchaFactory.GeneratedCaptcha generated = HutoolCaptchaFactory.generate(captchaPolicy);
        String challengeId = otpChallengeService.issueCaptchaCode(
                new OtpIssueCommand(
                        command.securityRealm(),
                        command.purpose(),
                        OtpChannel.CAPTCHA,
                        command.clientKey(),
                        null),
                generated.code());
        return new CaptchaIssueResult(
                challengeId,
                generated.imageBase64(),
                mimeType(captchaPolicy.style()),
                captchaPolicy.style(),
                otpChallengeService.findChallengeExpiresAt(challengeId));
    }

    /**
     * 校验 {@code captchaCode}；失败返回 {@code false}。
     */
    public boolean verify(CaptchaVerifyCommand command) {
        Checks.notNull(command, "command");
        return otpChallengeService.verify(new OtpVerifyCommand(
                command.securityRealm(),
                command.purpose(),
                OtpChannel.CAPTCHA,
                command.clientKey(),
                command.captchaCode()));
    }

    /**
     * 校验 {@code captchaCode}；失败抛出 {@link com.innospots.nexus.console.credential.otp.status.OtpStatusCode}。
     */
    public void verifyOrThrow(CaptchaVerifyCommand command) {
        Checks.notNull(command, "command");
        otpChallengeService.verifyOrThrow(new OtpVerifyCommand(
                command.securityRealm(),
                command.purpose(),
                OtpChannel.CAPTCHA,
                command.clientKey(),
                command.captchaCode()));
    }

    /**
     * 作废当前客户端键下未消费的图形验证码挑战。
     */
    public void invalidate(CaptchaVerifyCommand command) {
        Checks.notNull(command, "command");
        otpChallengeService.invalidate(
                command.securityRealm(),
                command.purpose(),
                OtpChannel.CAPTCHA,
                command.clientKey());
    }

    private static String mimeType(CaptchaStyle style) {
        if (style == CaptchaStyle.GIF) {
            return "image/gif";
        }
        return "image/png";
    }
}
