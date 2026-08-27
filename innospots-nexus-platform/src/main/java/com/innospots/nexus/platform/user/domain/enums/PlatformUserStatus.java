package com.innospots.nexus.platform.user.domain.enums;

/**
 * Platform-user lifecycle status.
 */
public enum PlatformUserStatus {

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
    LOCKED
}
