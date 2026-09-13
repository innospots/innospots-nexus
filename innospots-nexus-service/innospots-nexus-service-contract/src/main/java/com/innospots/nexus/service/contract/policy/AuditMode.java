package com.innospots.nexus.service.contract.policy;

/**
 * Audit persistence mode. {@link #REQUIRED} fails the invocation when audit cannot be written.
 *
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 * @see com.innospots.nexus.service.contract.audit.annotation.Audited
 */
public enum AuditMode {
    BEST_EFFORT,
    REQUIRED
}
