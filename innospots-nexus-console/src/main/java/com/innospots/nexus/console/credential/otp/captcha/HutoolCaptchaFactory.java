package com.innospots.nexus.console.credential.otp.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;
import cn.hutool.captcha.GifCaptcha;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.ShearCaptcha;

/**
 * 基于 {@code hutool-all}（{@code cn.hutool.captcha}）的图形验证码生成。
 */
public final class HutoolCaptchaFactory {

    /**
     * @param code         明文验证码（入库前须规范化）
     * @param imageBase64  PNG 或 GIF 的 Base64 载荷（不含 {@code data:} 前缀）
     */
    public record GeneratedCaptcha(String code, String imageBase64) {
    }

    private HutoolCaptchaFactory() {
    }

    /**
     * 按策略生成验证码图片与明文。
     *
     * @param policy 绘制参数
     * @return 明文与 Base64 图片
     */
    public static GeneratedCaptcha generate(CaptchaPolicy policy) {
        CaptchaPolicy effective = policy == null ? CaptchaPolicy.DEFAULT : policy;
        return switch (effective.style()) {
            case LINE -> fromLine(CaptchaUtil.createLineCaptcha(
                    effective.width(),
                    effective.height(),
                    effective.codeCount(),
                    effective.interferenceCount()));
            case CIRCLE -> fromCircle(CaptchaUtil.createCircleCaptcha(
                    effective.width(),
                    effective.height(),
                    effective.codeCount(),
                    effective.interferenceCount()));
            case SHEAR -> fromShear(CaptchaUtil.createShearCaptcha(
                    effective.width(),
                    effective.height(),
                    effective.codeCount(),
                    effective.interferenceCount()));
            case GIF -> fromGif(CaptchaUtil.createGifCaptcha(
                    effective.width(),
                    effective.height(),
                    effective.codeCount()));
        };
    }

    private static GeneratedCaptcha fromLine(LineCaptcha captcha) {
        return new GeneratedCaptcha(readCode(captcha), captcha.getImageBase64Data());
    }

    private static GeneratedCaptcha fromCircle(CircleCaptcha captcha) {
        return new GeneratedCaptcha(readCode(captcha), captcha.getImageBase64Data());
    }

    private static GeneratedCaptcha fromShear(ShearCaptcha captcha) {
        return new GeneratedCaptcha(readCode(captcha), captcha.getImageBase64Data());
    }

    private static GeneratedCaptcha fromGif(GifCaptcha captcha) {
        return new GeneratedCaptcha(readCode(captcha), captcha.getImageBase64Data());
    }

    private static String readCode(cn.hutool.captcha.AbstractCaptcha captcha) {
        return captcha.getCode();
    }
}
