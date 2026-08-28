package com.innospots.nexus.core.plugin.event;

import java.util.function.Consumer;

/**
 * Synchronous, best-effort event channel scoped to one plugin manager.
 */
public interface PluginEventBus {

    /**
     * Registers a typed event observer.
     *
     * @param eventType event class accepted by the handler
     * @param handler observer invoked on the publishing thread
     * @param <E> event type
     * @return idempotent subscription handle
     */
    <E extends PluginEvent> Subscription subscribe(Class<E> eventType, Consumer<E> handler);

    /**
     * Publishes an observation without propagating observer failures.
     *
     * @param event event to publish; {@code null} is ignored
     */
    void publish(PluginEvent event);
}
