package com.innospots.nexus.service.contract.audit;

/**
 * Audit storage that participates in the host's current transaction.
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditStorage
 * @see AuditEvent
 */
public interface TransactionalAuditStorage {

    /**
     * Appends {@code event} in the current transaction before business commit.
     *
     * @param event audit event
     */
    void appendInCurrentTransaction(AuditEvent event);
}
