package com.innospots.nexus.platform.settings.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 平台自助注册模式设置相关状态码（模块 {@code PRG}）。
 *
 * @author Smars
 * @date 2026/10/06
 */
public enum PlatformRegistrationModeSettingStatusCode implements StatusCode {

    REGISTRATION_MODE_DISABLED(
            "0001",
            StatusCategory.BUSINESS_RULE,
            "Registration is not enabled for the current platform mode",
            403),
    REGISTRATION_MODE_INVALID(
            "0002",
            StatusCategory.INPUT_VALIDATION,
            "Registration mode is invalid",
            400);

    private static final String MODULE = "PRG";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformRegistrationModeSettingStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check platform registration settings", "zh", "请检查平台注册模式设置");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
