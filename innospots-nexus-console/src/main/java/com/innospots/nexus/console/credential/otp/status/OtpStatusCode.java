package com.innospots.nexus.console.credential.otp.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * OTP 子域业务状态码（模块标识 {@code OTP}，完整码形如 {@code OTP-0001}）。
 * <p>由 {@link com.innospots.nexus.console.credential.otp.service.OtpChallengeService} 在挑战生命周期各阶段抛出。</p>
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.service.OtpChallengeService
 */
public enum OtpStatusCode implements StatusCode {

    /**
     * 无匹配未消费挑战（地址/用途/通道不一致或从未发放）。
     */
    CHALLENGE_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "OTP challenge was not found", 404),

    /**
     * 挑战已过 {@link com.innospots.nexus.console.credential.otp.domain.entity.OtpChallengeEntity#getExpiresAt()}。
     */
    CHALLENGE_EXPIRED("0002", StatusCategory.RESOURCE_DATA, "OTP challenge has expired", 400),

    /**
     * 挑战已被消费或作废（{@code consumedAt} 非空）。
     */
    CHALLENGE_CONSUMED("0003", StatusCategory.RESOURCE_DATA, "OTP challenge was already used", 400),

    /**
     * 验证码不匹配，或错误次数已达上限。
     */
    CODE_INVALID("0004", StatusCategory.PERMISSION_SECURITY, "OTP code is invalid", 400),

    /**
     * 距上次发放仍在 {@link com.innospots.nexus.console.credential.otp.policy.OtpPolicy#resendCooldown()} 内。
     */
    RESEND_TOO_FREQUENT("0005", StatusCategory.DATA_CONSISTENCY, "OTP resend is too frequent", 429);

    private static final String MODULE = "OTP";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    OtpStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of("en", message, "zh", message);
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String module() {
        return MODULE;
    }

    @Override
    public StatusCategory category() {
        return category;
    }

    @Override
    public String localCode() {
        return localCode;
    }

    @Override
    public I18nObject message() {
        return message;
    }

    @Override
    public I18nObject advice() {
        return I18nObject.of("en", "Request a new code or wait before retrying", "zh", "请重新获取验证码或稍后重试");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
