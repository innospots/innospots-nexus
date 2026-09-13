package com.innospots.nexus.service.contract.governance;

import java.util.Optional;

/**
 * Semaphore-style bulkhead. Missing policy keys fail as configuration errors.
 *
 * @author Smars
 * @date 2026/09/13
 * @see BulkheadPermit
 */
public interface BulkheadProvider {

    /**
     * Attempts to acquire a permit for {@code policyKey}.
     *
     * @param policyKey bulkhead policy key
     * @return permit or empty when exhausted
     */
    Optional<BulkheadPermit> tryAcquire(String policyKey);
}
