package com.innospots.nexus.kernel.user.domain.enums;

/**
 * User lifecycle status.
 */
public enum UserStatus {

    /**
     * Normal operational state.
     */
    ACTIVE,

    /**
     * Disabled by an administrator.
     */
    DISABLED,

    /**
     * Temporarily locked due to policy or repeated failures.
     */
    LOCKED,

    /**
     * Awaiting activation or verification.
     */
    PENDING
}
