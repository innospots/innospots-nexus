package com.innospots.nexus.service.runtime.context;

import java.util.concurrent.Callable;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * Captures the current {@link ServiceContext} and reinstalls it around delegated work.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ThreadBoundServiceContext
 * @see ContextExecutor
 */
public final class ContextPropagation {

    private final ThreadBoundServiceContext contexts;

    /**
     * Creates a propagator bound to {@code contexts}.
     *
     * @param contexts thread-bound accessor
     */
    public ContextPropagation(ThreadBoundServiceContext contexts) {
        this.contexts = Checks.notNull(contexts, "contexts");
    }

    /**
     * Wraps {@code runnable} so the captured context is installed for the duration of the run.
     *
     * @param runnable work to wrap
     * @return wrapped runnable
     */
    public Runnable wrap(Runnable runnable) {
        Checks.notNull(runnable, "runnable");
        ServiceContext captured = contexts.current().orElse(null);
        return () -> runWith(captured, runnable);
    }

    /**
     * Wraps {@code callable} so the captured context is installed for the duration of the call.
     *
     * @param callable work to wrap
     * @param <V>      result type
     * @return wrapped callable
     */
    public <V> Callable<V> wrap(Callable<V> callable) {
        Checks.notNull(callable, "callable");
        ServiceContext captured = contexts.current().orElse(null);
        return () -> {
            ContextSnapshot snapshot = captured == null ? null : contexts.install(captured);
            try {
                return callable.call();
            } finally {
                restore(snapshot);
            }
        };
    }

    private void runWith(ServiceContext captured, Runnable runnable) {
        ContextSnapshot snapshot = captured == null ? null : contexts.install(captured);
        try {
            runnable.run();
        } finally {
            restore(snapshot);
        }
    }

    private void restore(ContextSnapshot snapshot) {
        if (snapshot != null) {
            contexts.restore(snapshot);
        }
    }
}
