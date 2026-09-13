package com.innospots.nexus.service.runtime.invocation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * CAS terminal-state holder for logical outcome, work termination, and transport completion.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationEngine
 * @see InvocationOutcome
 */
public final class InvocationControl {

    private final AtomicReference<InvocationOutcome> logicalOutcome = new AtomicReference<>();
    private final AtomicBoolean workTerminated = new AtomicBoolean();
    private final AtomicBoolean transportCompleted = new AtomicBoolean();

    /**
     * Completes the caller-visible outcome at most once.
     *
     * @param outcome logical outcome
     * @return {@code true} when this call stored the outcome
     */
    public boolean completeLogical(InvocationOutcome outcome) {
        Checks.notNull(outcome, "outcome");
        return logicalOutcome.compareAndSet(null, outcome);
    }

    /**
     * Marks work termination at most once.
     *
     * @return {@code true} when this call performed termination
     */
    public boolean terminateWork() {
        return workTerminated.compareAndSet(false, true);
    }

    /**
     * Marks transport completion at most once.
     *
     * @return {@code true} when this call recorded transport completion
     */
    public boolean completeTransport() {
        return transportCompleted.compareAndSet(false, true);
    }

    /**
     * Returns the logical outcome when completed.
     *
     * @return outcome or empty
     */
    public Optional<InvocationOutcome> logicalOutcome() {
        return Optional.ofNullable(logicalOutcome.get());
    }
}
