package com.innospots.nexus.console.auth.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.console.credential.otp.captcha.CaptchaStyle;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueResult;

/**
 * 登录图形验证码发放结果（REST 形状）。
 *
 * @param challengeId   挑战 ID
 * @param clientKey     校验登录时需回传的客户端键（与发放时一致）
 * @param imageBase64   图片 Base64（不含 data URI 前缀）
 * @param imageMimeType 建议 MIME
 * @param style         绘制样式
 * @param expiresAt     过期时间
 */
public record AuthCaptchaVo(
        String challengeId,
        String clientKey,
        String imageBase64,
        String imageMimeType,
        CaptchaStyle style,
        LocalDateTime expiresAt
) {

    /**
     * 从凭据域发放结果构造；{@code clientKey} 与发放命令一致。
     */
    public static AuthCaptchaVo from(CaptchaIssueResult result, String clientKey) {
        return new AuthCaptchaVo(
                result.challengeId(),
                clientKey,
                result.imageBase64(),
                result.imageMimeType(),
                result.style(),
                result.expiresAt());
    }
}
