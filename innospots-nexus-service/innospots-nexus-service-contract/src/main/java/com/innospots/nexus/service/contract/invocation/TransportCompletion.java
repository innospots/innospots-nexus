package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * Observes transport-level completion for streaming or async invocations.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationOutcome
 */
@FunctionalInterface
public interface TransportCompletion {

    /**
     * Returns the stage completed when the transport finishes.
     *
     * @return completion outcome
     */
    CompletionStage<InvocationOutcome> completion();
}
