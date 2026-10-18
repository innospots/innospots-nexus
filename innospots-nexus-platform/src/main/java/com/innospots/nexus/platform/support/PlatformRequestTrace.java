package com.innospots.nexus.platform.support;

import com.innospots.nexus.base.thread.TLC;

/**
 * 平台公开自助流程日志中的请求跟踪 ID（与 {@link TLC#TRACE_ID} / X-Request-Id 一致）。
 */
public final class PlatformRequestTrace {

    private static final String MISSING = "-";

    private PlatformRequestTrace() {
    }

    /**
     * 当前线程跟踪 ID；未注入过滤器上下文时返回 {@code "-"}。
     */
    public static String currentTraceId() {
        String traceId = TLC.getString(TLC.TRACE_ID);
        if (traceId == null || traceId.isBlank()) {
            return MISSING;
        }
        return traceId;
    }

    /**
     * 登录名用于日志（去首尾空白，不写密码）。
     */
    public static String safeLogin(String login) {
        if (login == null || login.isBlank()) {
            return MISSING;
        }
        return login.trim();
    }

    /**
     * 注册 OTP / 联系方式摘要（掩码，便于排障且不落明文）。
     */
    public static String describeContact(String email, String mobile) {
        if (email != null && !email.isBlank()) {
            return "EMAIL:" + maskEmail(email.trim());
        }
        if (mobile != null && !mobile.isBlank()) {
            return "MOBILE:" + maskMobile(mobile.trim());
        }
        return MISSING;
    }

    /**
     * 单字段联系方式（邀请码注册等），按是否含 {@code @} 区分邮箱或手机。
     */
    public static String describeContactValue(String contact) {
        if (contact == null || contact.isBlank()) {
            return MISSING;
        }
        String trimmed = contact.trim();
        if (trimmed.contains("@")) {
            return "EMAIL:" + maskEmail(trimmed);
        }
        return "MOBILE:" + maskMobile(trimmed);
    }

    private static String maskEmail(String email) {
        if (!email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    private static String maskMobile(String mobile) {
        if (mobile.length() < 4) {
            return "***";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 2);
    }
}
