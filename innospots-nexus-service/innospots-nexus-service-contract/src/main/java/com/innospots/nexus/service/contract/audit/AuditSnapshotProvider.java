package com.innospots.nexus.service.contract.audit;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * Captures a safe audit snapshot from invocation input and result.
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditSnapshot
 */
public interface AuditSnapshotProvider {

    /**
     * Captures before/after maps. Must not retain entity or credential references.
     *
     * @param context invocation
     * @param input   business input
     * @param result  business result, possibly null
     * @return snapshot
     */
    AuditSnapshot capture(InvocationContext context, Object input, Object result);
}
