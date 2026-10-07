package com.innospots.nexus.platform.invite.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 平台邀请域状态码。
 */
public enum PlatformInviteStatusCode implements StatusCode {

    INVITE_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Platform invite was not found", 404),
    INVITE_NOT_PENDING("0002", StatusCategory.BUSINESS_RULE, "Invite is not pending", 409),
    INVITE_EXPIRED("0003", StatusCategory.BUSINESS_RULE, "Invite has expired", 410),
    INVITE_CONTACT_MISMATCH("0004", StatusCategory.BUSINESS_RULE, "Contact does not match invite", 400),
    INVITE_CODE_LOCKED("0005", StatusCategory.PERMISSION_SECURITY, "Invite code is locked", 423),
    INVITE_CONTACT_REQUIRED("0006", StatusCategory.INPUT_VALIDATION, "Email or mobile is required", 400),
    INVITE_DEFAULT_ROLE_NOT_FOUND(
            "0007",
            StatusCategory.RESOURCE_DATA,
            "Default role code was not found for platform realm",
            404);

    private static final String MODULE = "PIN";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PlatformInviteStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check invite token or code", "zh", "请检查邀请令牌或邀请码");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
