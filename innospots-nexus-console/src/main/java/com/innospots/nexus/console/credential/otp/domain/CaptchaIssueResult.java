package com.innospots.nexus.console.credential.otp.domain;

import java.time.LocalDateTime;

import com.innospots.nexus.console.credential.otp.captcha.CaptchaStyle;

/**
 * 图形验证码发放结果。
 *
 * @param challengeId   挑战主键，校验时与 {@code clientKey} 一起定位记录
 * @param imageBase64   PNG/GIF 的 Base64（不含 data URI 前缀）
 * @param imageMimeType 建议的 MIME，例如 {@code image/png} 或 {@code image/gif}
 * @param style         实际使用的绘制样式
 * @param expiresAt     挑战过期时间（服务端时钟）
 */
public record CaptchaIssueResult(
        String challengeId,
        String imageBase64,
        String imageMimeType,
        CaptchaStyle style,
        LocalDateTime expiresAt
) {
}
