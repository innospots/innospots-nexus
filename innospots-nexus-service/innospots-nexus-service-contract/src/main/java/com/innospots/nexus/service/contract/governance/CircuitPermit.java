package com.innospots.nexus.service.contract.governance;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * Circuit permit that must receive a terminal outcome.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CircuitBreakerProvider
 */
@FunctionalInterface
public interface CircuitPermit {

    /**
     * Reports the invocation outcome to the circuit.
     *
     * @param outcome terminal outcome
     */
    void finish(InvocationOutcome outcome);
}
