package com.innospots.nexus.platform.auth.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 运营管理平台认证域状态码（模块 {@code PAF}）。
 *
 * @author Smars
 * @date 2026/10/06
 */
public enum PlatformAuthStatusCode implements StatusCode {

    PASSWORD_RESET_VERIFICATION_INVALID(
            "0001",
            StatusCategory.PERMISSION_SECURITY,
            "Password reset verification code is invalid",
            400);

    private static final String MODULE = "PAF";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformAuthStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of("en", message, "zh", message);
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String module() {
        return MODULE;
    }

    @Override
    public StatusCategory category() {
        return category;
    }

    @Override
    public String localCode() {
        return localCode;
    }

    @Override
    public I18nObject message() {
        return message;
    }

    @Override
    public I18nObject advice() {
        if (this == PASSWORD_RESET_VERIFICATION_INVALID) {
            return I18nObject.of(
                    "en",
                    "Request a new verification code and try again",
                    "zh",
                    "请重新获取验证码后再试");
        }
        return I18nObject.of("en", "Check authentication request", "zh", "请检查认证请求");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
