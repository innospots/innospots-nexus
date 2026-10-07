package com.innospots.nexus.console.credential.otp.captcha;

/**
 * 图形验证码绘制参数（与 {@link com.innospots.nexus.console.credential.otp.policy.OtpPolicy} 的 TTL/重试策略独立）。
 *
 * @param width              图片宽度（像素）
 * @param height             图片高度（像素）
 * @param codeCount          验证码字符个数
 * @param interferenceCount  干扰线/圆数量（GIF 样式忽略）
 * @param style              绘制样式，默认 {@link CaptchaStyle#LINE}
 */
public record CaptchaPolicy(
        int width,
        int height,
        int codeCount,
        int interferenceCount,
        CaptchaStyle style
) {

    /**
     * 控制台推荐：200×100、4 位字符、线段干扰。
     */
    public static final CaptchaPolicy DEFAULT = new CaptchaPolicy(200, 100, 4, 100, CaptchaStyle.LINE);

    public CaptchaPolicy {
        if (width < 80 || height < 30) {
            throw new IllegalArgumentException("captcha image size too small");
        }
        if (codeCount < 4 || codeCount > 8) {
            throw new IllegalArgumentException("codeCount must be between 4 and 8");
        }
        if (interferenceCount < 0) {
            throw new IllegalArgumentException("interferenceCount must not be negative");
        }
        if (style == null) {
            style = CaptchaStyle.LINE;
        }
    }

    /**
     * 使用默认 {@link CaptchaStyle#LINE} 的便捷构造。
     */
    public CaptchaPolicy(int width, int height, int codeCount, int interferenceCount) {
        this(width, height, codeCount, interferenceCount, CaptchaStyle.LINE);
    }
}
