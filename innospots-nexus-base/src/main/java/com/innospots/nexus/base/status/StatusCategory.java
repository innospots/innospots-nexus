package com.innospots.nexus.base.status;

/**
 * Categorises status codes by concern area. Each category has a 2-digit
 * code embedded in the full status code string and a priority level
 * (L=Low, M=Medium, H=High, B=Blocker, C=Critical).
 */
public enum StatusCategory {
    GENERAL("00", "General", "L"),
    INPUT_VALIDATION("01", "Input validation", "H"),
    BUSINESS_RULE("02", "Business rule", "H"),
    INTERNAL_ERROR("03", "Internal error", "C"),
    PERMISSION_SECURITY("04", "Permission or security", "C"),
    TRANSACTION_CONFLICT("05", "Transaction conflict", "M"),
    EXTERNAL_FAILURE("06", "External failure", "C"),
    DATA_CONSISTENCY("07", "Data consistency", "C"),
    CONFIGURATION("08", "Configuration", "H"),
    COMPLIANCE("09", "Compliance", "B"),
    RESOURCE_LIMIT("10", "Resource limit", "C"),
    BATCH_JOB("11", "Batch job", "H"),
    CHANNEL_INTERACTION("12", "Channel interaction", "M"),
    RESOURCE_DATA("13", "Resource data", "M"),
    DATA_OPERATION("14", "Data operation", "M"),
    FILE_OPERATION("15", "File operation", "M"),
    MIDDLEWARE("16", "Middleware", "C"),
    CRYPTO("17", "Cryptography", "C"),
    SCRIPT("18", "Script", "C"),
    DATA_CONNECTION("19", "Data connection", "C"),
    DATA_SCHEMA("20", "Data schema", "C"),
    SQL_EXECUTION("21", "SQL execution", "C");

    private final String code;
    private final String label;
    private final String priority;

    StatusCategory(String code, String label, String priority) {
        this.code = code;
        this.label = label;
        this.priority = priority;
    }

    public String code() {
        return code;
    }

    public String label() {
        return label;
    }

    public String priority() {
        return priority;
    }
}
