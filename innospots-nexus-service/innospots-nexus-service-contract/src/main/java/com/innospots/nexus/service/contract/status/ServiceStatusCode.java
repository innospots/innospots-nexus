package com.innospots.nexus.service.contract.status;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.status.StatusCategory;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.status.StatusCodeRules;

/**
 * 服务框架状态码。本地码在 SRV 模块内唯一。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.status.NexusStatusCode
 * @see com.innospots.nexus.base.exception.NexusException
 */
public enum ServiceStatusCode implements StatusCode {

    CONTEXT_UNAVAILABLE("0001", StatusCategory.CONFIGURATION, 500, false,
            "Service context unavailable", "服务上下文不可用",
            "Contact the service operator", "请联系服务管理员"),
    DEADLINE_EXCEEDED("0002", StatusCategory.RESOURCE_LIMIT, 504, false,
            "Deadline exceeded", "执行已超时",
            "Retry after reducing work or increasing timeout", "请降低工作量或增大超时后重试"),
    BUFFER_OVERFLOW("0003", StatusCategory.RESOURCE_LIMIT, 503, false,
            "Buffer capacity exceeded", "缓冲容量已满",
            "Slow down producers or increase bounded buffer", "请降低生产速率或增大有界缓冲"),
    CAPACITY_EXHAUSTED("0004", StatusCategory.RESOURCE_LIMIT, 503, true,
            "Service capacity exhausted", "服务容量不足",
            "Retry after the advertised delay", "请按提示稍后重试"),
    CIRCUIT_OPEN("0005", StatusCategory.EXTERNAL_FAILURE, 503, true,
            "Downstream temporarily unavailable", "下游暂不可用",
            "Retry after the advertised delay", "请按提示稍后重试"),
    PAYLOAD_TOO_LARGE("0006", StatusCategory.INPUT_VALIDATION, 413, false,
            "Payload too large", "数据超过大小限制",
            "Check the request size limits", "请检查请求大小限制"),
    MEDIA_TYPE_REJECTED("0007", StatusCategory.INPUT_VALIDATION, 415, false,
            "Media type not allowed", "媒体类型不允许",
            "Check the request", "请检查请求"),
    LIFECYCLE_CLOSED("0008", StatusCategory.CHANNEL_INTERACTION, 409, false,
            "Session is closed", "会话已关闭",
            "Open a new session", "请新建会话"),
    MESSAGE_TYPE_UNKNOWN("0009", StatusCategory.CHANNEL_INTERACTION, 400, false,
            "Unknown message type", "未知消息类型",
            "Check the request", "请检查请求"),
    RANGE_NOT_SATISFIABLE("0010", StatusCategory.FILE_OPERATION, 416, false,
            "Requested range unavailable", "请求范围不可用",
            "Check the requested range", "请检查请求范围"),
    UPLOAD_UNSAFE("0011", StatusCategory.PERMISSION_SECURITY, 422, false,
            "Upload rejected by security policy", "上传被安全策略拒绝",
            "Check access credentials and upload policy", "请检查访问凭据与上传策略"),
    AUDIT_UNAVAILABLE("0012", StatusCategory.COMPLIANCE, 503, false,
            "Required audit unavailable", "必需审计不可用",
            "Contact the service operator", "请联系服务管理员"),
    IDEMPOTENCY_CONFLICT("0013", StatusCategory.TRANSACTION_CONFLICT, 409, false,
            "Idempotency key conflict", "幂等键冲突",
            "Check the idempotency key and retry policy", "请检查幂等键与重试策略"),
    OPERATION_CANCELLED("0014", StatusCategory.CHANNEL_INTERACTION, 499, false,
            "Operation cancelled", "操作已取消",
            "Repeat the operation if still required", "如仍需要请重新发起操作"),
    CONTENT_READ_FAILED("0015", StatusCategory.FILE_OPERATION, 502, false,
            "Resource read failed", "资源读取失败",
            "Contact the service operator", "请联系服务管理员"),
    SCANNER_UNAVAILABLE("0016", StatusCategory.EXTERNAL_FAILURE, 503, false,
            "Upload scanner unavailable", "上传扫描不可用",
            "Retry after the advertised delay", "请按提示稍后重试"),
    CONTENT_CAPABILITY_MISSING("0017", StatusCategory.FILE_OPERATION, 501, false,
            "Streaming storage capability unavailable", "存储流式能力不可用",
            "Contact the service operator", "请联系服务管理员"),
    DOWNSTREAM_FAILED("0018", StatusCategory.EXTERNAL_FAILURE, 502, false,
            "Downstream call failed", "下游调用失败",
            "Retry after the advertised delay", "请按提示稍后重试"),
    PRECONDITION_FAILED("0019", StatusCategory.TRANSACTION_CONFLICT, 412, false,
            "Resource precondition failed", "资源前置条件不满足",
            "Reload the resource and retry", "请重新加载资源后重试");

    private static final String MODULE = "SRV";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final I18nObject advice;
    private final int httpStatusCode;
    private final boolean retryable;

    ServiceStatusCode(
            String localCode,
            StatusCategory category,
            int httpStatusCode,
            boolean retryable,
            String messageEn,
            String messageZh,
            String adviceEn,
            String adviceZh) {
        StatusCodeRules.requireValid(MODULE, category, localCode);
        this.localCode = localCode;
        this.category = category;
        this.httpStatusCode = httpStatusCode;
        this.retryable = retryable;
        this.message = I18nObject.of("en", messageEn, "zh", messageZh);
        this.advice = I18nObject.of("en", adviceEn, "zh", adviceZh);
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
        return advice;
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }

    /**
     * 在其他条件允许时，返回调用方是否可在此失败后重试。
     *
     * @return 可能安全重试时为 {@code true}
     */
    public boolean retryable() {
        return retryable;
    }
}
