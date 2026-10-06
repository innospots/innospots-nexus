package com.innospots.nexus.platform.access.support;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;

/**
 * 注册流程中 OTP 发码/验码共用的联系方式解析（APPROVAL 与 OPEN）。
 */
public final class PlatformRegistrationContactSupport {

    private PlatformRegistrationContactSupport() {
    }

    public static void requireContact(String email, String mobile) {
        if (!hasText(email) && !hasText(mobile)) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_CONTACT_REQUIRED);
        }
    }

    /**
     * 提交注册时 OTP 校验键：优先邮箱，否则手机（与发码规则一致）。
     */
    public static String resolveVerificationContact(String email, String mobile) {
        requireContact(email, mobile);
        if (hasText(email)) {
            return email.trim();
        }
        return mobile.trim();
    }

    /**
     * 发码请求：若携带 {@code email}/{@code mobile} 则与提交表单同一规则；否则兼容仅传 {@code contact}。
     */
    public static String resolveVerificationContactForOtpRequest(String email, String mobile, String legacyContact) {
        if (hasText(email) || hasText(mobile)) {
            return resolveVerificationContact(email, mobile);
        }
        Checks.notBlank(legacyContact, "contact");
        return legacyContact.trim();
    }

    public static OtpChannel resolveChannel(String contact) {
        if (contact.contains("@")) {
            return OtpChannel.EMAIL;
        }
        return OtpChannel.MOBILE;
    }

    /**
     * OPEN 注册登录名：显式填写，或从邮箱本地段推导；仅手机时必须提供 loginName。
     */
    public static String resolveOpenRegistrationLoginName(String loginName, String email, String mobile) {
        if (hasText(loginName)) {
            return loginName.trim();
        }
        if (hasText(email) && email.trim().contains("@")) {
            String normalized = email.trim().toLowerCase();
            return normalized.substring(0, normalized.indexOf('@'));
        }
        if (hasText(mobile) && !hasText(email)) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_LOGIN_NAME_REQUIRED);
        }
        throw NexusException.build(PlatformAccessStatusCode.ACCESS_LOGIN_NAME_REQUIRED);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
