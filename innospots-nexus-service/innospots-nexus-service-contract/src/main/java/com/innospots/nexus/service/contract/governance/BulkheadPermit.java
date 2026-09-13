package com.innospots.nexus.service.contract.governance;

/**
 * Bulkhead permit that must be closed after use.
 *
 * @author Smars
 * @date 2026/09/13
 * @see BulkheadProvider
 */
@FunctionalInterface
public interface BulkheadPermit extends AutoCloseable {

    /**
     * Releases the permit. Repeated closes are idempotent.
     */
    @Override
    void close();
}
