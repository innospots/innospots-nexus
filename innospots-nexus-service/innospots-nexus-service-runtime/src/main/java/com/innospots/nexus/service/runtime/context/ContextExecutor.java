package com.innospots.nexus.service.runtime.context;

import java.util.concurrent.Executor;

import com.innospots.nexus.base.util.Checks;

/**
 * Executor that propagates the captured {@link com.innospots.nexus.service.contract.context.ServiceContext}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ContextPropagation
 * @see ThreadBoundServiceContext
 */
public final class ContextExecutor implements Executor {

    private final Executor delegate;
    private final ContextPropagation propagation;

    /**
     * Creates an executor that wraps tasks with {@code propagation}.
     *
     * @param delegate     underlying executor
     * @param propagation  context propagator
     */
    public ContextExecutor(Executor delegate, ContextPropagation propagation) {
        this.delegate = Checks.notNull(delegate, "delegate");
        this.propagation = Checks.notNull(propagation, "propagation");
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(propagation.wrap(Checks.notNull(command, "command")));
    }
}
