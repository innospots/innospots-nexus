package com.innospots.nexus.core.plugin.event;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.resource.ResourceScope;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Instance-local synchronous event bus that isolates observer failures.
 */
public final class DefaultPluginEventBus implements PluginEventBus {

    private final CopyOnWriteArrayList<HandlerRegistration<?>> handlers = new CopyOnWriteArrayList<>();
    private final System.Logger logger = System.getLogger(DefaultPluginEventBus.class.getName());
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Registers a synchronous observer until its subscription or owning resource scope is closed.
     *
     * @param eventType event class accepted by the handler
     * @param handler observer invoked on the publishing thread
     * @param <E> event type
     * @return idempotent subscription handle
     * @throws NexusException when this bus is closed or an input is missing
     */
    @Override
    public <E extends PluginEvent> Subscription subscribe(Class<E> eventType, Consumer<E> handler) {
        if (closed.get()) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_STOP_FAILED,
                    "plugin event bus is already closed");
        }
        if (eventType == null || handler == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "event type and handler are required");
        }
        HandlerRegistration<E> registration = new HandlerRegistration<>(eventType, handler, handlers);
        handlers.add(registration);
        return registration;
    }

    /**
     * Publishes an event to a stable observer snapshot and isolates observer failures.
     *
     * @param event event to publish; {@code null} is ignored
     */
    @Override
    public void publish(PluginEvent event) {
        if (event == null || closed.get()) {
            return;
        }
        // CopyOnWriteArrayList iteration is already a stable snapshot and never holds a lifecycle lock.
        for (HandlerRegistration<?> registration : handlers) {
            try {
                registration.accept(event);
            } catch (RuntimeException exception) {
                logger.log(System.Logger.Level.WARNING, "Plugin event observer failed", exception);
            }
        }
    }

    /** Closes this runtime-local bus and releases all subscriber references. */
    public void close() {
        if (closed.compareAndSet(false, true)) {
            handlers.clear();
        }
    }

    /**
     * Returns a view whose subscriptions are automatically owned by the supplied plugin scope.
     *
     * @param scope resource scope that owns every subscription created through the view
     * @return scoped event bus view
     * @throws NexusException when the scope is {@code null}
     */
    public PluginEventBus scoped(ResourceScope scope) {
        if (scope == null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_START_FAILED,
                    "plugin resource scope is required");
        }
        return new PluginEventBus() {
            @Override
            public <E extends PluginEvent> Subscription subscribe(Class<E> eventType, Consumer<E> handler) {
                Subscription subscription = DefaultPluginEventBus.this.subscribe(eventType, handler);
                try {
                    scope.add(subscription::close);
                    return subscription;
                } catch (RuntimeException exception) {
                    // Registration is already visible in the bus, so undo it if scope ownership cannot be established.
                    subscription.close();
                    throw exception;
                }
            }

            @Override
            public void publish(PluginEvent event) {
                DefaultPluginEventBus.this.publish(event);
            }
        };
    }

    private static final class HandlerRegistration<E extends PluginEvent> implements Subscription {

        private final Class<E> eventType;
        private final Consumer<E> handler;
        private final CopyOnWriteArrayList<HandlerRegistration<?>> owner;
        private final AtomicBoolean closed = new AtomicBoolean();

        private HandlerRegistration(
                Class<E> eventType,
                Consumer<E> handler,
                CopyOnWriteArrayList<HandlerRegistration<?>> owner
        ) {
            this.eventType = eventType;
            this.handler = handler;
            this.owner = owner;
        }

        private void accept(PluginEvent event) {
            if (!closed.get() && eventType.isInstance(event)) {
                handler.accept(eventType.cast(event));
            }
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.remove(this);
            }
        }
    }
}
