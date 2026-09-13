package com.innospots.nexus.service.contract.audit;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * Append-only audit storage. Completion means the implementation's persistence confirmation.
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditEvent
 * @see TransactionalAuditStorage
 */
public interface AuditStorage {

    /**
     * Appends {@code event}. Duplicate {@code eventId} values are ignored.
     *
     * @param event audit event
     * @return completion
     */
    CompletionStage<Void> append(AuditEvent event);

    /**
     * Flushes buffered events within {@code timeout}.
     *
     * @param timeout flush timeout
     * @return completion
     */
    CompletionStage<Void> flush(Duration timeout);
}
