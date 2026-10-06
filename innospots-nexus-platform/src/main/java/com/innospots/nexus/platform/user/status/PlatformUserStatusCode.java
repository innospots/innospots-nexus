package com.innospots.nexus.platform.user.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 平台用户域状态码（模块 {@code PLU}）。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum PlatformUserStatusCode implements StatusCode {

    PLATFORM_USER_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Platform user was not found", 404),
    LOGIN_NAME_DUPLICATED("0002", StatusCategory.DATA_CONSISTENCY, "Login name already exists", 409),
    PLATFORM_USER_NOT_PENDING_APPROVAL(
            "0003",
            StatusCategory.BUSINESS_RULE,
            "Platform user is not pending registration approval",
            409);

    private static final String MODULE = "PLU";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformUserStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check platform user identifier", "zh", "请检查平台用户标识");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
