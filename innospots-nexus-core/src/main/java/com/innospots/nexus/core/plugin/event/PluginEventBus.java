package com.innospots.nexus.core.plugin.event;

import java.util.function.Consumer;

/**
 * Synchronous, best-effort event channel scoped to one plugin manager.
 */
public interface PluginEventBus {

    /** Registers a typed event observer. */
    <E extends PluginEvent> Subscription subscribe(Class<E> eventType, Consumer<E> handler);

    /** Publishes an observation without propagating observer failures. */
    void publish(PluginEvent event);
}
