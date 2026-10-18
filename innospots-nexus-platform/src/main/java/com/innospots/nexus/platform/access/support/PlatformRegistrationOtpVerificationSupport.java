package com.innospots.nexus.platform.access.support;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.config.RegistrationOtpVerification;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;

/**
 * 按 {@link RegistrationConfig#getOtpVerification()} 校验注册请求中的联系方式与 OTP 字段是否必填。
 *
 * @author Smars
 * @date 2026/10/18
 * @see com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService
 */
public final class PlatformRegistrationOtpVerificationSupport {

    private PlatformRegistrationOtpVerificationSupport() {
    }

    /**
     * 断言当前配置要求 OTP；用于「获取验证码」类 API。
     *
     * @param registrationConfig 注册配置
     * @throws NexusException {@link RegistrationOtpVerification#NONE} 时 {@link PlatformAccessStatusCode#ACCESS_OTP_NOT_REQUIRED}
     */
    public static void requireOtpAllowed(RegistrationConfig registrationConfig) {
        Checks.notNull(registrationConfig, "registrationConfig");
        if (!registrationConfig.isOtpRequired()) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_OTP_NOT_REQUIRED);
        }
    }

    /**
     * 按 OTP 方式校验邮箱或手机是否已提供。
     *
     * @param registrationConfig 注册配置
     * @param email              邮箱
     * @param mobile             手机
     */
    public static void requireContactForVerification(
            RegistrationConfig registrationConfig,
            String email,
            String mobile
    ) {
        Checks.notNull(registrationConfig, "registrationConfig");
        RegistrationOtpVerification verification = registrationConfig.getOtpVerification();
        if (verification == RegistrationOtpVerification.NONE) {
            return;
        }
        if (verification == RegistrationOtpVerification.EMAIL) {
            Checks.notBlank(email, "email");
            return;
        }
        if (verification == RegistrationOtpVerification.MOBILE) {
            Checks.notBlank(mobile, "mobile");
        }
    }

    /**
     * 在需要 OTP 时校验验证码非空。
     *
     * @param registrationConfig 注册配置
     * @param verificationCode     用户输入的验证码
     */
    public static void requireVerificationCodeIfNeeded(RegistrationConfig registrationConfig, String verificationCode) {
        if (registrationConfig.isOtpRequired()) {
            Checks.notBlank(verificationCode, "verificationCode");
        }
    }

    /**
     * 将 OTP 域重发过频错误映射为平台注册域状态码。
     *
     * @param ex 原始异常
     */
    public static void mapOtpResendException(NexusException ex) {
        if (OtpStatusCode.RESEND_TOO_FREQUENT.fullCode().equals(ex.code())) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_OTP_RESEND_TOO_FREQUENT);
        }
        throw ex;
    }
}
