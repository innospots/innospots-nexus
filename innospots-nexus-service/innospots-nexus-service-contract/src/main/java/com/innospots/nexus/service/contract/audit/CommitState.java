package com.innospots.nexus.service.contract.audit;

/**
 * Host transaction commit state observed by audit.
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
