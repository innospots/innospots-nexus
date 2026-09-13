package com.innospots.nexus.service.contract.governance;

/**
 * Circuit breaker that rejects immediately when open.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CircuitPermit
 */
public interface CircuitBreakerProvider {

    /**
     * Acquires a permit for {@code policyKey}. Open circuits fail with SRV060005.
     *
     * @param policyKey circuit policy key
     * @return permit
     */
    CircuitPermit acquire(String policyKey);
}
