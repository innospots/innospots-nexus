package com.innospots.nexus.kernel.domain;

/**
 * Kernel-owned business domains for the management platform foundation.
 */
public enum KernelDomain {

    AUTH("auth"),
    MENU("menu"),
    AUDIT_LOG("audit-log"),
    USER("user"),
    USER_GROUP("user-group"),
    ROLE("role"),
    PERMISSION("permission"),
    DICTIONARY("dictionary"),
    NOTIFICATION("notification"),
    SYSTEM_CONFIG("system-config"),
    I18N("i18n"),
    CATEGORY("category");

    private final String code;

    KernelDomain(String code) {
        this.code = code;
    }

    /**
     * Returns the stable domain code used in declarations and logs.
     *
     * @return stable domain code
     */
    public String code() {
        return code;
    }
}
