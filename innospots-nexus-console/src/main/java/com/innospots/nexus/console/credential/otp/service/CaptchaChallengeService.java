package com.innospots.nexus.console.credential.otp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(CaptchaChallengeService.class);

    private final OtpChallengeService otpChallengeService;

    /**
     * 是否把验证码明文写进 INFO 日志，仅供本地/联调排查。
     *
     * <p>来源为 {@code nexus.console.auth.login-captcha-debug-log-enabled}。
     * 登录图形验证码不经过事件总线（{@code publishSendEvent=false}），
     * {@code RegistrationOtpDebugLogger} 收不到它；后者只覆盖注册时的邮件/短信 OTP。
     * 生产必须保持 {@code false}。</p>
     */
    private final boolean debugLogEnabled;

    public CaptchaChallengeService(OtpChallengeService otpChallengeService) {
        this(otpChallengeService, false);
    }

    /**
     * @param otpChallengeService 挑战服务
     * @param debugLogEnabled     是否在 INFO 日志输出验证码明文（仅本地/联调）
     */
    public CaptchaChallengeService(OtpChallengeService otpChallengeService, boolean debugLogEnabled) {
        this.otpChallengeService = Checks.notNull(otpChallengeService, "otpChallengeService");
        this.debugLogEnabled = debugLogEnabled;
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
        if (debugLogEnabled) {
            log.info(
                    "Login captcha debug challengeId={} realm={} purpose={} clientKey={} code={}",
                    challengeId,
                    command.securityRealm(),
                    command.purpose(),
                    command.clientKey(),
                    generated.code());
        }
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
