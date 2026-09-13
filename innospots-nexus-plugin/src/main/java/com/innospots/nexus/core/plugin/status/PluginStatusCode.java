package com.innospots.nexus.core.plugin.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * 轻量级插件运行时产生的状态码。
 *
 * <p>所有状态码归属 {@code PLG} 模块，HTTP 状态由 {@link #httpStatusCode()} 提供。</p>
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
    CAPABILITY_TYPE_MISMATCH("0013", StatusCategory.INTERNAL_ERROR, "Capability type does not match", 500),
    PROVIDER_DUPLICATE("0014", StatusCategory.CONFIGURATION, "Provider identifier is duplicated", 500),
    DSL_SYNTAX_INVALID("0015", StatusCategory.CONFIGURATION, "Plugin DSL syntax is invalid", 400),
    DSL_STRUCTURE_INVALID("0016", StatusCategory.CONFIGURATION, "Plugin DSL structure is invalid", 400),
    CAPABILITY_TYPE_UNKNOWN("0017", StatusCategory.CONFIGURATION, "Capability type is unknown", 400),
    UNSUPPORTED_BIND_KIND("0018", StatusCategory.CONFIGURATION, "Plugin bind kind is unsupported", 400),
    UNSUPPORTED_EXPOSURE_KIND("0019", StatusCategory.CONFIGURATION, "Plugin exposure kind is unsupported", 400),
    UNSUPPORTED_CONTRIBUTION_TYPE("0020", StatusCategory.CONFIGURATION, "Plugin contribution type is unsupported", 400),
    RESOURCE_CONFLICT("0021", StatusCategory.DATA_CONSISTENCY, "Plugin resource identity conflicts", 409),
    PLUGIN_CONCURRENCY_CONFLICT("0022", StatusCategory.TRANSACTION_CONFLICT, "Plugin command conflicts", 409),
    PLUGIN_NOT_INSTALLED("0023", StatusCategory.RESOURCE_DATA, "Plugin is not installed", 409),
    PLUGIN_MISSING("0024", StatusCategory.RESOURCE_DATA, "Plugin is missing", 404),
    PLUGIN_PERSISTENCE_FAILED("0025", StatusCategory.INTERNAL_ERROR, "Plugin persistence failed", 500);

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
