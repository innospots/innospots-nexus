package com.innospots.nexus.platform.settings.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 控制台界面设置（主题、导航布局）相关状态码（模块 {@code PRG}）。
 */
public enum PlatformConsoleUiSettingStatusCode implements StatusCode {

    CONSOLE_THEME_INVALID(
            "0010",
            StatusCategory.INPUT_VALIDATION,
            "Console theme is invalid",
            400),
    NAVIGATION_LAYOUT_INVALID(
            "0011",
            StatusCategory.INPUT_VALIDATION,
            "Navigation layout mode is invalid",
            400),
    CONSOLE_SITE_NAME_INVALID(
            "0012",
            StatusCategory.INPUT_VALIDATION,
            "Console site name is invalid",
            400),
    CONSOLE_SITE_DESCRIPTION_INVALID(
            "0013",
            StatusCategory.INPUT_VALIDATION,
            "Console site description is invalid",
            400),
    CONSOLE_DISPLAY_LANGUAGE_INVALID(
            "0014",
            StatusCategory.INPUT_VALIDATION,
            "Console display language is invalid",
            400),
    CONSOLE_TIMEZONE_INVALID(
            "0015",
            StatusCategory.INPUT_VALIDATION,
            "Console timezone is invalid",
            400);

    private static final String MODULE = "PRG";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformConsoleUiSettingStatusCode(
            String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check console UI settings", "zh", "请检查控制台界面设置");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
