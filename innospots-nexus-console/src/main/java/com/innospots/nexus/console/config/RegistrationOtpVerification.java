package com.innospots.nexus.console.config;

/**
 * 注册流程身份校验方式（部署配置项 {@code nexus.console.registration.otp-verification}）。
 *
 * @author Smars
 * @date 2026/10/18
 * @see RegistrationConfig#otpVerification
 */
public enum RegistrationOtpVerification {

    /** 向邮箱发送 OTP，注册表单须填写邮箱并校验验证码。 */
    EMAIL,

    /** 向手机发送 OTP（经 {@link RegistrationConfig.OtpDelivery#mobile} 配置的 HTTP API）。 */
    MOBILE,

    /** 不发送 OTP；开放注册等流程仅校验登录名与密码。 */
    NONE
}
