package com.innospots.nexus.service.contract.observation;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * Starts invocation observations. Label cardinality is owned by the implementation.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationObservation
 */
public interface MetricsProvider {

    /**
     * Begins observation for {@code invocation}.
     *
     * @param invocation current invocation
     * @return observation
     */
    InvocationObservation begin(InvocationContext invocation);
}
