package com.innospots.nexus.platform.access.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 平台注册待审核域状态码（模块 {@code PAC}）。
 *
 * @author Smars
 * @date 2026/10/06
 */
public enum PlatformAccessStatusCode implements StatusCode {

    ACCESS_REQUEST_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Access request was not found", 404),
    ACCESS_REQUEST_NOT_PENDING("0002", StatusCategory.BUSINESS_RULE, "Access request is not pending", 409),
    ACCESS_CONTACT_REQUIRED("0003", StatusCategory.INPUT_VALIDATION, "Email or mobile is required", 400),
    ACCESS_OTP_RESEND_TOO_FREQUENT(
            "0004",
            StatusCategory.BUSINESS_RULE,
            "Verification code resend is too frequent",
            429),
    ACCESS_LOGIN_NAME_REQUIRED(
            "0005",
            StatusCategory.INPUT_VALIDATION,
            "Login name is required when email is not provided",
            400);

    private static final String MODULE = "PAC";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformAccessStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        if (this == ACCESS_OTP_RESEND_TOO_FREQUENT) {
            return I18nObject.of(
                    "en",
                    "Wait at least 60 seconds before requesting another code",
                    "zh",
                    "验证码发送过于频繁，请至少间隔 60 秒后再试");
        }
        if (this == ACCESS_LOGIN_NAME_REQUIRED) {
            return I18nObject.of(
                    "en",
                    "Provide loginName or register with an email address",
                    "zh",
                    "请填写登录名，或使用邮箱注册以便系统自动生成登录名");
        }
        return I18nObject.of("en", "Check access request identifier", "zh", "请检查访问申请标识");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
