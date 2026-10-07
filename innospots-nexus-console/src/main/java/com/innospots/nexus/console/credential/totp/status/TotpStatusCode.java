package com.innospots.nexus.console.credential.totp.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * TOTP/MFA 子域 {@link com.innospots.nexus.base.status.StatusCode}（模块 {@code MFA}）。
 *
 * @author Smars
 * @date 2026/09/19
 */
public enum TotpStatusCode implements StatusCode {

    NOT_ENROLLED("0001", StatusCategory.RESOURCE_DATA, "TOTP is not enrolled for this user", 404),
    ENROLLMENT_PENDING("0002", StatusCategory.DATA_CONSISTENCY, "TOTP enrollment is pending confirmation", 400),
    CODE_INVALID("0003", StatusCategory.PERMISSION_SECURITY, "TOTP code is invalid", 400),
    ALREADY_ENROLLED("0004", StatusCategory.DATA_CONSISTENCY, "TOTP is already enrolled", 409);

    private static final String MODULE = "MFA";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    TotpStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Complete enrollment or use a valid authenticator code", "zh", "请完成绑定或使用正确的动态码");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
