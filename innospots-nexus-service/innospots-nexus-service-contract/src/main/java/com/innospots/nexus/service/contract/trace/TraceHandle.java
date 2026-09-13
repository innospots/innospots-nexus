package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * Active span handle created by {@link TraceProvider}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see TraceProvider
 */
public interface TraceHandle {

    /**
     * Returns the snapshot for the active span.
     *
     * @return current snapshot
     */
    TraceSnapshot snapshot();

    /**
     * Completes the span with the invocation outcome.
     *
     * @param outcome terminal outcome
     */
    void finish(InvocationOutcome outcome);
}
