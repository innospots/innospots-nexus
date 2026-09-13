package com.innospots.nexus.service.contract.cancellation;

import java.util.Optional;
import java.util.function.Consumer;

import com.innospots.nexus.base.util.Checks;

/**
 * Read-only cancellation signal for the current invocation.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationReason
 * @see CancellationRegistration
 */
public interface CancellationToken {

    /**
     * Returns whether cancellation has been requested.
     *
     * @return {@code true} when cancelled
     */
    boolean isCancelled();

    /**
     * Returns the cancellation reason when cancelled.
     *
     * @return reason or empty
     */
    Optional<CancellationReason> reason();

    /**
     * Registers a listener invoked at most once when cancelled. If already cancelled, the listener
     * is invoked immediately on the calling thread.
     *
     * @param listener cancellation callback
     * @return registration that can be closed to unsubscribe
     */
    CancellationRegistration onCancel(Consumer<CancellationReason> listener);

    /**
     * Returns a token that is never cancelled.
     *
     * @return never-cancelled token
     */
    static CancellationToken none() {
        return NeverCancelledToken.INSTANCE;
    }
}

final class NeverCancelledToken implements CancellationToken {

    static final NeverCancelledToken INSTANCE = new NeverCancelledToken();

    private NeverCancelledToken() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public Optional<CancellationReason> reason() {
        return Optional.empty();
    }

    @Override
    public CancellationRegistration onCancel(Consumer<CancellationReason> listener) {
        Checks.notNull(listener, "listener");
        return () -> {
        };
    }
}
