package com.innospots.nexus.console.dictionary.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 字典域状态码。
 */
public enum DictionaryStatusCode implements StatusCode {

    TYPE_NOT_FOUND("0001", StatusCategory.RESOURCE_DATA, "Dictionary type was not found", 404),
    TYPE_CODE_DUPLICATED("0002", StatusCategory.DATA_CONSISTENCY, "Dictionary type code already exists", 409),
    TYPE_PROTECTED("0003", StatusCategory.PERMISSION_SECURITY, "Built-in dictionary type cannot be deleted", 403),
    ITEM_NOT_FOUND("0004", StatusCategory.RESOURCE_DATA, "Dictionary item was not found", 404),
    ITEM_VALUE_DUPLICATED("0005", StatusCategory.DATA_CONSISTENCY, "Dictionary item value already exists", 409),
    ITEM_PROTECTED("0006", StatusCategory.PERMISSION_SECURITY, "Built-in dictionary item cannot be deleted", 403);

    private static final String MODULE = "DIC";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    DictionaryStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check dictionary identifier and tenant scope", "zh", "请检查字典标识与租户范围");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
