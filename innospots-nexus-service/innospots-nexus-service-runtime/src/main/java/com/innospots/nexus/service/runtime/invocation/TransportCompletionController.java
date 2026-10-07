package com.innospots.nexus.service.runtime.invocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.TransportCompletion;

/**
 * 精确一次完成传输层结果。
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
     * 在尚未完成时完成传输结果。
     *
     * @param outcome 传输结果
     * @return 本次调用完成阶段时为 {@code true}
     */
    public boolean complete(InvocationOutcome outcome) {
        Checks.notNull(outcome, "outcome");
        return completion.complete(outcome);
    }
}
