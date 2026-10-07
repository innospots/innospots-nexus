package com.innospots.nexus.base.status;

/**
 * 按关注领域对状态码进行分类。每个分类在完整状态码字符串中嵌入 2 位数字码，
 * 并附带优先级级别（L=低、M=中、H=高、B=阻塞、C=严重）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see StatusCode
 * @see NexusStatusCode
 */
public enum StatusCategory {
    /** 通用 */
    GENERAL("00", "General", "L"),
    /** 输入校验 */
    INPUT_VALIDATION("01", "Input validation", "H"),
    /** 业务规则 */
    BUSINESS_RULE("02", "Business rule", "H"),
    /** 内部错误 */
    INTERNAL_ERROR("03", "Internal error", "C"),
    /** 权限或安全 */
    PERMISSION_SECURITY("04", "Permission or security", "C"),
    /** 事务冲突 */
    TRANSACTION_CONFLICT("05", "Transaction conflict", "M"),
    /** 外部故障 */
    EXTERNAL_FAILURE("06", "External failure", "C"),
    /** 数据一致性 */
    DATA_CONSISTENCY("07", "Data consistency", "C"),
    /** 配置 */
    CONFIGURATION("08", "Configuration", "H"),
    /** 合规 */
    COMPLIANCE("09", "Compliance", "B"),
    /** 资源限制 */
    RESOURCE_LIMIT("10", "Resource limit", "C"),
    /** 批处理作业 */
    BATCH_JOB("11", "Batch job", "H"),
    /** 渠道交互 */
    CHANNEL_INTERACTION("12", "Channel interaction", "M"),
    /** 资源数据 */
    RESOURCE_DATA("13", "Resource data", "M"),
    /** 数据操作 */
    DATA_OPERATION("14", "Data operation", "M"),
    /** 文件操作 */
    FILE_OPERATION("15", "File operation", "M"),
    /** 中间件 */
    MIDDLEWARE("16", "Middleware", "C"),
    /** 密码学 */
    CRYPTO("17", "Cryptography", "C"),
    /** 脚本 */
    SCRIPT("18", "Script", "C"),
    /** 数据连接 */
    DATA_CONNECTION("19", "Data connection", "C"),
    /** 数据模式 */
    DATA_SCHEMA("20", "Data schema", "C"),
    /** SQL 执行 */
    SQL_EXECUTION("21", "SQL execution", "C");

    private final String code;
    private final String label;
    private final String priority;

    StatusCategory(String code, String label, String priority) {
        this.code = code;
        this.label = label;
        this.priority = priority;
    }

    /**
     * 返回 2 位数字分类码。
     *
     * @return 分类码
     */
    public String code() {
        return code;
    }

    /**
     * 返回分类标签。
     *
     * @return 标签
     */
    public String label() {
        return label;
    }

    /**
     * 返回优先级级别。
     *
     * @return 优先级（L/M/H/B/C）
     */
    public String priority() {
        return priority;
    }
}
