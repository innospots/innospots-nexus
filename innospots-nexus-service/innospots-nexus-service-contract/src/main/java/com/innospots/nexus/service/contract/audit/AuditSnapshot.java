package com.innospots.nexus.service.contract.audit;

import java.util.Map;

/**
 * Safe before/after snapshots for audit. Values must already be JSON-compatible trees.
 *
 * @param before previous state
 * @param after  next state
 * @author Smars
 * @date 2026/09/13
 * @see AuditSnapshotProvider
 */
public record AuditSnapshot(Map<String, Object> before, Map<String, Object> after) {

    public AuditSnapshot {
        before = before == null ? Map.of() : Map.copyOf(before);
        after = after == null ? Map.of() : Map.copyOf(after);
    }
}
