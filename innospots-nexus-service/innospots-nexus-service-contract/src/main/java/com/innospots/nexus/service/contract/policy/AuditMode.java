package com.innospots.nexus.service.contract.policy;

/**
 * 审计持久化模式。{@link #REQUIRED} 在无法写入审计时使调用失败。
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
