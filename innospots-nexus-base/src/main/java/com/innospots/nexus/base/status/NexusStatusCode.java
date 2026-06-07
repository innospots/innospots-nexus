package com.innospots.nexus.base.status;

import com.innospots.nexus.base.i18n.I18nObject;

import java.util.Arrays;
import java.util.Optional;

/**
 * Platform-wide status codes with bilingual (EN/ZH) messages and advice,
 * grouped by {@link StatusCategory}. Each code has a 6-character full code
 * in the format {@code NEX + category(2) + local(4)}.
 */
public enum NexusStatusCode implements StatusCode {
    SUCCESS("0000", StatusCategory.GENERAL,
            "Success", "成功",
            "", "",
            200),
    INVALID_PARAMETER("0001", StatusCategory.INPUT_VALIDATION,
            "Invalid parameter", "参数无效",
            "Please check the request parameter", "请检查请求参数",
            400),
    CONFIG_ERROR("0002", StatusCategory.CONFIGURATION,
            "Configuration error", "配置错误",
            "Please check the configuration", "请检查配置",
            500),
    SERIALIZATION_FAILED("0003", StatusCategory.INTERNAL_ERROR,
            "Serialization failed", "序列化失败",
            "Please check the data structure or payload", "请检查数据结构或数据内容",
            500),
    DATA_NOT_FOUND("0004", StatusCategory.DATA_OPERATION,
            "Data not found", "数据不存在",
            "Please check whether the data exists", "请检查数据是否存在",
            404),
    RESOURCE_NOT_FOUND("0005", StatusCategory.RESOURCE_DATA,
            "Resource not found", "资源不存在",
            "Please check whether the resource exists", "请检查资源是否存在",
            404),
    NO_PERMISSION("0006", StatusCategory.PERMISSION_SECURITY,
            "No permission", "没有权限",
            "Please check the access permission", "请检查访问权限",
            403),
    AUTHENTICATION_FAILED("0007", StatusCategory.PERMISSION_SECURITY,
            "Authentication failed", "认证失败",
            "Please sign in again or check the credential", "请重新登录或检查认证凭据",
            401),
    PASSWORD_ERROR("0008", StatusCategory.PERMISSION_SECURITY,
            "Password error", "密码错误",
            "Please check the password", "请检查密码",
            400),
    USER_NOT_FOUND("0009", StatusCategory.PERMISSION_SECURITY,
            "User not found", "用户不存在",
            "Please check whether the user exists", "请检查用户是否存在",
            404),
    EXECUTION_FAILED("0010", StatusCategory.BATCH_JOB,
            "Execution failed", "执行失败",
            "Please check the execution context", "请检查执行上下文",
            500),
    BUSINESS_ERROR("0011", StatusCategory.BUSINESS_RULE,
            "Business error", "业务逻辑错误",
            "Please check the business rule", "请检查业务规则",
            400),
    LIMIT_EXCEEDED("0012", StatusCategory.RESOURCE_LIMIT,
            "Limit exceeded", "超出限制",
            "Please check the limit configuration", "请检查限制配置",
            429),
    RETRY_FAILED("0013", StatusCategory.EXTERNAL_FAILURE,
            "Retry failed", "重试失败",
            "Please check the retry target", "请检查重试目标",
            500),
    OPTIMISTIC_LOCK_FAILED("0014", StatusCategory.TRANSACTION_CONFLICT,
            "Optimistic lock failed", "乐观锁失败",
            "Please reload the latest data and retry", "请重新加载最新数据后重试",
            409),
    COMPILE_FAILED("0015", StatusCategory.SCRIPT,
            "Compile failed", "编译失败",
            "Please check the script or expression", "请检查脚本或表达式",
            500),
    INITIALIZATION_FAILED("0016", StatusCategory.INTERNAL_ERROR,
            "Initialization failed", "初始化失败",
            "Please check the initialization configuration", "请检查初始化配置",
            500),
    SYSTEM_ERROR("0017", StatusCategory.INTERNAL_ERROR,
            "System error", "系统错误",
            "Please contact the administrator or retry later", "请联系管理员或稍后重试",
            500);

    private static final String MODULE = "NEX";

    private final String localCode;
    private final StatusCategory category;
    private final I18nObject message;
    private final I18nObject advice;
    private final int httpStatusCode;

    NexusStatusCode(String localCode,
                    StatusCategory category,
                    String messageEn,
                    String messageZh,
                    String adviceEn,
                    String adviceZh,
                    int httpStatusCode) {
        StatusCodeRules.requireValid(MODULE, category, localCode);
        this.localCode = localCode;
        this.category = category;
        this.message = I18nObject.of("en", messageEn, "zh", messageZh);
        this.advice = I18nObject.of("en", adviceEn, "zh", adviceZh);
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
        return advice;
    }

    @Override
    public int httpStatusCode() {
        return httpStatusCode;
    }

    /**
     * Looks up a status code by its full code string (e.g. {@code NEX000000}).
     *
     * @param fullCode the full 7-character code
     * @return the matching enum constant, or empty if not found
     */
    public static Optional<NexusStatusCode> findByFullCode(String fullCode) {
        return Arrays.stream(values())
                .filter(value -> value.fullCode().equals(fullCode))
                .findFirst();
    }
}
