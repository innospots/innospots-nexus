package com.innospots.nexus.core.plugin.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * Status codes emitted by the lightweight plugin runtime.
 */
public enum PluginStatusCode implements StatusCode {

    PLUGIN_DISCOVERY_FAILED("0001", StatusCategory.INTERNAL_ERROR, "Plugin discovery failed", 500),
    PLUGIN_DEFINITION_INVALID("0002", StatusCategory.CONFIGURATION, "Plugin definition is invalid", 500),
    PLUGIN_DUPLICATE("0003", StatusCategory.CONFIGURATION, "Plugin identifier is duplicated", 500),
    PLUGIN_API_INCOMPATIBLE("0004", StatusCategory.CONFIGURATION, "Plugin API is incompatible", 500),
    PLUGIN_CONFIG_INVALID("0005", StatusCategory.CONFIGURATION, "Plugin configuration is invalid", 500),
    PLUGIN_DEPENDENCY_MISSING("0006", StatusCategory.CONFIGURATION, "Plugin dependency is missing", 500),
    PLUGIN_DEPENDENCY_CYCLE("0007", StatusCategory.CONFIGURATION, "Plugin dependency cannot be resolved", 500),
    PLUGIN_START_FAILED("0008", StatusCategory.INTERNAL_ERROR, "Plugin start failed", 500),
    PLUGIN_STOP_FAILED("0009", StatusCategory.INTERNAL_ERROR, "Plugin stop failed", 500),
    PLUGIN_IN_USE("0010", StatusCategory.TRANSACTION_CONFLICT, "Plugin is still required", 409),
    CAPABILITY_NOT_FOUND("0011", StatusCategory.RESOURCE_DATA, "Capability was not found", 404),
    CAPABILITY_AMBIGUOUS("0012", StatusCategory.DATA_CONSISTENCY, "Capability selection is ambiguous", 409),
    CAPABILITY_TYPE_MISMATCH("0013", StatusCategory.INTERNAL_ERROR, "Capability type does not match", 500);

    private static final String MODULE = "PLG";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    PluginStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check plugin diagnostics", "zh", "请检查插件诊断信息");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
