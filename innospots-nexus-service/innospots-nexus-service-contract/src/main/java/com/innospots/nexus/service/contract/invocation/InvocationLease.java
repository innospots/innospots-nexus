package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * Lease obtained from {@link ServiceInterceptor#enter(InvocationContext)}. Must be finished once.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInterceptor
 * @see InvocationOutcome
 */
@FunctionalInterface
public interface InvocationLease {

    /**
     * Completes the lease with {@code outcome}. Repeated finishes are ignored by runtime.
     *
     * @param outcome terminal outcome
     * @return completion stage
     */
    CompletionStage<Void> finish(InvocationOutcome outcome);
}
