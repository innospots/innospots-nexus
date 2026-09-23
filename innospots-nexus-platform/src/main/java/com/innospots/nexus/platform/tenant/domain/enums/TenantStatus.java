package com.innospots.nexus.platform.tenant.domain.enums;

/**
 * 平台管理租户的生命周期状态。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum TenantStatus {

    /**
     * 租户处于运营状态。
     */
    ACTIVE,

    /**
     * 租户访问被临时阻断。
     */
    SUSPENDED,

    /**
     * 租户保留但不再活跃。
     */
    ARCHIVED
}
