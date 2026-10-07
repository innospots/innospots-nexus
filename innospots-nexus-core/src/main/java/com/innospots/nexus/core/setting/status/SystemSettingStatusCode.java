package com.innospots.nexus.core.setting.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 系统设置域状态码（模块 {@code SET}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.core.setting.service.SystemSettingService
 */
public enum SystemSettingStatusCode implements StatusCode {

    /** 取值不符合 {@link com.innospots.nexus.core.setting.domain.enums.SettingValueType} 对应格式。 */
    SETTING_VALUE_INVALID(
            "0001",
            StatusCategory.INPUT_VALIDATION,
            "Setting value is invalid",
            400),

    /** 超过 {@link com.innospots.nexus.core.setting.domain.entity.SystemSettingEntity#SETTING_VALUE_MAX_LENGTH}。 */
    SETTING_VALUE_TOO_LONG(
            "0002",
            StatusCategory.INPUT_VALIDATION,
            "Setting value exceeds maximum length",
            400),

    /** 库内 {@code value_type} 与 {@link com.innospots.nexus.core.setting.support.SettingKey} 声明不一致。 */
    SETTING_VALUE_TYPE_MISMATCH(
            "0003",
            StatusCategory.INPUT_VALIDATION,
            "Setting value type does not match the registered key",
            400);

    private static final String MODULE = "SET";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    SystemSettingStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check setting value format", "zh", "请检查设置项取值格式");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
