package com.innospots.nexus.console.logger.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 审计日志域状态码。
 */
public enum AuditLogStatusCode implements StatusCode {

    NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Audit log was not found", 404);

    private static final String MODULE = "ALG";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    AuditLogStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check audit log id and platform scope", "zh", "请检查日志 ID 与平台会话范围");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
