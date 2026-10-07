package com.innospots.nexus.service.contract.audit;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * 从调用入参与结果捕获安全审计快照。
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditSnapshot
 */
public interface AuditSnapshotProvider {

    /**
     * 捕获前后映射。不得保留实体或凭证引用。
     *
     * @param context 调用上下文
     * @param input   业务入参
     * @param result  业务结果，可能为 null
     * @return 快照
     */
    AuditSnapshot capture(InvocationContext context, Object input, Object result);
}
