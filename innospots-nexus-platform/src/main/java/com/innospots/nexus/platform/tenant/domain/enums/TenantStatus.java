package com.innospots.nexus.platform.tenant.domain.enums;

/**
 * Lifecycle status of a platform-managed tenant.
 */
public enum TenantStatus {

    /** Tenant is operational. */
    ACTIVE,

    /** Tenant access is temporarily blocked. */
    SUSPENDED,

    /** Tenant is retained but no longer active. */
    ARCHIVED
}
