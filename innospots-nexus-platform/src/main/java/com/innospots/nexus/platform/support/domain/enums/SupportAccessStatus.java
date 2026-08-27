package com.innospots.nexus.platform.support.domain.enums;

/**
 * Lifecycle of a platform support-access grant into a tenant.
 */
public enum SupportAccessStatus {

    /** Waiting for tenant-admin approval. */
    PENDING,

    /** Currently usable until {@code expireAt}. */
    ACTIVE,

    /** Past expiry. */
    EXPIRED,

    /** Explicitly revoked. */
    REVOKED
}
