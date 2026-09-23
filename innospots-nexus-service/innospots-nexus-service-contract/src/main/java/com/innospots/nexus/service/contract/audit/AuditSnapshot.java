package com.innospots.nexus.service.contract.audit;

import java.util.Map;

/**
 * 审计用的安全前后快照。值必须为 JSON 兼容树结构。
 *
 * @param before 变更前状态
 * @param after  变更后状态
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
