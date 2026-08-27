package com.innospots.nexus.kernel.member.domain.enums;

/**
 * Lifecycle status of a tenant membership.
 */
public enum TenantMemberStatus {

    /** Member can access the tenant. */
    ACTIVE,

    /** Member is blocked from tenant access. */
    DISABLED,

    /** Invitation or join is not yet completed. */
    PENDING
}
