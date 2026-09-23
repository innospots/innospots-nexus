package com.innospots.nexus.console.credential.otp.domain.enums;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.credential.password.VerificationType;

/**
 * OTP 下发通道；决定地址规范化规则、{@link com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent#eventType()}
 * 后缀，以及与 legacy {@link VerificationType} 的互转（仅 EMAIL/MOBILE）。
 *
 * @author Smars
 * @date 2026/09/19
 * @see com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent#channel()
 */
public enum OtpChannel {

    /**
     * 电子邮件；地址规范化为 trim + 小写。
     */
    EMAIL,

    /**
     * 短信/语音等手机号通道；地址规范化为去空格。
     */
    MOBILE,

    /**
     * 向租户配置的 Webhook URL 投递（扩展通道）。
     */
    WEBHOOK,

    /**
     * 应用内消息（站内信等），无 legacy {@link VerificationType} 映射。
     */
    IN_APP,

    /**
     * 图形验证码；{@code destination} 为客户端会话键，明文码经 API 以图片返回而非事件投递。
     */
    CAPTCHA;

    /**
     * 从 kernel 使用的 {@link VerificationType} 转为 OTP 通道。
     * <p>调用场景：忘记密码流程仅支持邮箱与手机。</p>
     *
     * @param type 验证类型
     * @return 对应通道
     * @throws IllegalArgumentException 非 EMAIL/MOBILE 时
     */
    public static OtpChannel fromVerificationType(VerificationType type) {
        if (type == VerificationType.EMAIL) {
            return EMAIL;
        }
        if (type == VerificationType.MOBILE) {
            return MOBILE;
        }
        throw new IllegalArgumentException("Unsupported verification type: " + type);
    }

    /**
     * 转回 {@link VerificationType}，供与旧 API 互操作。
     *
     * @return EMAIL 或 MOBILE
     * @throws IllegalArgumentException WEBHOOK、IN_APP 等无对应类型时
     */
    public VerificationType toVerificationType() {
        if (this == EMAIL) {
            return VerificationType.EMAIL;
        }
        if (this == MOBILE) {
            return VerificationType.MOBILE;
        }
        throw new IllegalArgumentException("No legacy VerificationType for channel: " + this);
    }

    /**
     * 规范化投递地址，作为挑战表查询键与重发冷却键。
     * <p>调用场景：{@link com.innospots.nexus.console.credential.otp.service.OtpChallengeService} 发放与校验前统一调用。</p>
     *
     * @param destination 用户输入的邮箱或手机号等
     * @return 规范化后的地址
     */
    public String normalizeDestination(String destination) {
        Checks.notBlank(destination, "destination");
        String trimmed = destination.trim();
        if (this == EMAIL) {
            return trimmed.toLowerCase();
        }
        if (this == MOBILE) {
            return trimmed.replace(" ", "");
        }
        if (this == CAPTCHA) {
            return trimmed;
        }
        return trimmed;
    }
}
