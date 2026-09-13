package com.innospots.nexus.service.runtime.invocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.TransportCompletion;

/**
 * Completes transport-level outcome exactly once.
 *
 * @author Smars
 * @date 2026/09/13
 * @see TransportCompletion
 * @see InvocationControl
 */
public final class TransportCompletionController implements TransportCompletion {

    private final CompletableFuture<InvocationOutcome> completion = new CompletableFuture<>();

    @Override
    public CompletionStage<InvocationOutcome> completion() {
        return completion;
    }

    /**
     * Completes the transport outcome when it has not already completed.
     *
     * @param outcome transport outcome
     * @return {@code true} when this call completed the stage
     */
    public boolean complete(InvocationOutcome outcome) {
        Checks.notNull(outcome, "outcome");
        return completion.complete(outcome);
    }
}
