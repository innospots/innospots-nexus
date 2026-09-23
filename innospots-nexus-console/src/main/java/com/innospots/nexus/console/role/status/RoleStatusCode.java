package com.innospots.nexus.console.role.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 角色与角色绑定域状态码。
 */
public enum RoleStatusCode implements StatusCode {

    ROLE_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Role was not found", 404),
    ROLE_CODE_DUPLICATED("0002", StatusCategory.DATA_CONSISTENCY, "Role code already exists", 409),
    ROLE_PROTECTED("0003", StatusCategory.PERMISSION_SECURITY, "Built-in role cannot be deleted", 403),
    ROLE_BINDING_NOT_FOUND("0004", StatusCategory.RESOURCE_DATA, "Role binding was not found", 404),
    INVALID_ROLE_OWNER("0005", StatusCategory.CONFIGURATION, "Role owner is invalid", 400);

    private static final String MODULE = "ROL";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    RoleStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check role identifier and owner scope", "zh", "请检查角色标识与归属范围");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
