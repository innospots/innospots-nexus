package com.innospots.nexus.service.contract.audit;

/**
 * 审计观察的主机事务提交状态。
 *
 * @author Smars
 * @date 2026/09/13
 * @see CommitObserver
 */
public enum CommitState {
    NO_TRANSACTION,
    ACTIVE,
    COMMITTED,
    ROLLED_BACK
}
