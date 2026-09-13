package com.innospots.nexus.service.runtime.cancellation;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;

/**
 * Write-side cancellation controller. The first cancel wins; listeners run at most once.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationToken
 * @see CancellationReason
 */
public final class CancellationSource {

    private static final Logger log = LoggerFactory.getLogger(CancellationSource.class);

    private final AtomicReference<CancellationReason> cancelled = new AtomicReference<>();
    private final List<Consumer<CancellationReason>> listeners = new CopyOnWriteArrayList<>();
    private final CancellationToken token = new SourceToken();

    /**
     * Requests cancellation. The first successful call returns {@code true}.
     *
     * @param reason cancellation reason
     * @return {@code true} when this call performed cancellation
     */
    public boolean cancel(CancellationReason reason) {
        Checks.notNull(reason, "reason");
        if (!cancelled.compareAndSet(null, reason)) {
            return false;
        }
        Error fatal = notifyAll(reason);
        if (fatal != null) {
            throw fatal;
        }
        return true;
    }

    /**
     * Returns the read-only token bound to this source.
     *
     * @return cancellation token
     */
    public CancellationToken token() {
        return token;
    }

    private CancellationRegistration register(Consumer<CancellationReason> listener) {
        Checks.notNull(listener, "listener");
        AtomicBoolean once = new AtomicBoolean();
        Consumer<CancellationReason> wrapped = reason -> {
            if (once.compareAndSet(false, true)) {
                listener.accept(reason);
            }
        };
        CancellationReason current = cancelled.get();
        if (current != null) {
            notifyOne(wrapped, current);
            return () -> {
            };
        }
        listeners.add(wrapped);
        current = cancelled.get();
        if (current != null && listeners.remove(wrapped)) {
            notifyOne(wrapped, current);
        }
        return () -> listeners.remove(wrapped);
    }

    private Error notifyAll(CancellationReason reason) {
        Error fatal = null;
        for (Consumer<CancellationReason> listener : listeners) {
            try {
                listener.accept(reason);
            } catch (RuntimeException ex) {
                log.warn("Cancellation listener failed", ex);
            } catch (Error error) {
                if (fatal == null) {
                    fatal = error;
                } else {
                    fatal.addSuppressed(error);
                }
            }
        }
        listeners.clear();
        return fatal;
    }

    private void notifyOne(Consumer<CancellationReason> listener, CancellationReason reason) {
        try {
            listener.accept(reason);
        } catch (RuntimeException ex) {
            log.warn("Cancellation listener failed", ex);
        }
    }

    private final class SourceToken implements CancellationToken {

        @Override
        public boolean isCancelled() {
            return cancelled.get() != null;
        }

        @Override
        public Optional<CancellationReason> reason() {
            return Optional.ofNullable(cancelled.get());
        }

        @Override
        public CancellationRegistration onCancel(Consumer<CancellationReason> listener) {
            return register(listener);
        }
    }
}
