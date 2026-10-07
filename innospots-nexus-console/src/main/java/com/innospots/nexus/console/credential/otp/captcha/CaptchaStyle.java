package com.innospots.nexus.console.credential.otp.captcha;

/**
 * Hutool 图形验证码样式，对应 {@code cn.hutool.captcha} 中的实现类。
 */
public enum CaptchaStyle {

    /**
     * 线段干扰（{@link cn.hutool.captcha.LineCaptcha}），默认推荐。
     */
    LINE,

    /**
     * 圆圈干扰（{@link cn.hutool.captcha.CircleCaptcha}）。
     */
    CIRCLE,

    /**
     * 字符扭曲（{@link cn.hutool.captcha.ShearCaptcha}）。
     */
    SHEAR,

    /**
     * 动态 GIF（{@link cn.hutool.captcha.GifCaptcha}）。
     */
    GIF
}
