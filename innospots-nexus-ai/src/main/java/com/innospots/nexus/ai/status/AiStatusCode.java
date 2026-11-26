package com.innospots.nexus.ai.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;

/**
 * AI runtime 状态码。
 */
public enum AiStatusCode implements StatusCode {

    RUNTIME_NOT_STARTED("0001", StatusCategory.CONFIGURATION, "Runtime is not started", 503),
    RUNTIME_START_FAILED("0002", StatusCategory.EXTERNAL_FAILURE, "Runtime failed to start", 503),
    RUNTIME_ALREADY_STARTED("0003", StatusCategory.CONFIGURATION, "Runtime is already started", 409),
    RUNTIME_UNHEALTHY("0004", StatusCategory.EXTERNAL_FAILURE, "Runtime is unhealthy", 503),
    RUNTIME_STOP_TIMEOUT("0005", StatusCategory.EXTERNAL_FAILURE, "Runtime stop timed out", 500),
    NODE_NOT_AVAILABLE("0006", StatusCategory.CONFIGURATION, "Node.js is not available", 500),
    RUNTIME_PACKAGE_MISSING("0007", StatusCategory.RESOURCE_DATA, "Runtime npm package is missing", 500),
    RUNTIME_VERSION_MISMATCH("0008", StatusCategory.CONFIGURATION, "Runtime package version does not match", 500),
    CLIENT_REQUEST_FAILED("0009", StatusCategory.EXTERNAL_FAILURE, "Upstream request failed", 502),
    PROXY_REQUEST_DENIED("0010", StatusCategory.PERMISSION_SECURITY, "Proxy request is not allowed", 403),
    INVALID_RUNTIME_CONFIG("0011", StatusCategory.CONFIGURATION, "Runtime configuration is invalid", 400),
    DATA_DIR_LOCKED("0012", StatusCategory.RESOURCE_LIMIT, "Data directory is locked", 409),
    PROXY_BODY_TOO_LARGE("0013", StatusCategory.INPUT_VALIDATION, "Request body is too large", 413),
    PROXY_UPSTREAM_UNAVAILABLE("0014", StatusCategory.EXTERNAL_FAILURE, "AI runtime is unavailable", 503),
    PROXY_UPSTREAM_TIMEOUT("0015", StatusCategory.EXTERNAL_FAILURE, "AI runtime request timed out", 504),
    PROXY_STREAM_FAILED("0016", StatusCategory.EXTERNAL_FAILURE, "AI runtime stream failed", 502),
    PROXY_REQUEST_INVALID("0017", StatusCategory.INPUT_VALIDATION, "Proxy request is invalid", 400),
    PROXY_METHOD_NOT_ALLOWED("0018", StatusCategory.INPUT_VALIDATION, "HTTP method is not allowed", 405),
    PROXY_PATH_NOT_FOUND("0019", StatusCategory.RESOURCE_DATA, "Proxy path was not found", 404),
    RUNTIME_STARTING("0020", StatusCategory.CONFIGURATION, "Runtime is starting", 503),
    RUNTIME_DEGRADED("0021", StatusCategory.EXTERNAL_FAILURE, "Runtime is degraded", 503),
    RUNTIME_CONFIG_INVALID("0022", StatusCategory.CONFIGURATION, "Runtime configuration is invalid", 503);

    private static final String MODULE = "NAI";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final int httpStatusCode;

    AiStatusCode(String localCode, StatusCategory category, String message, int httpStatusCode) {
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
        return I18nObject.of("en", "Check AI runtime configuration and logs",
                "zh", "请检查 AI 运行时配置与日志");
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }
}
