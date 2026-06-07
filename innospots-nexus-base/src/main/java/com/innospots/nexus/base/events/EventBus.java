package com.innospots.nexus.base.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory event bus. Subscribers register via {@link #subscribe} for
 * specific event types; publishers fire events via {@link #publish} (fire-and-forget)
 * or {@link #publishSync} (blocking, returns last handler result).
 * <p>Handlers are stored in {@link CopyOnWriteArrayList} for safe concurrent
 * iteration. Event-type matching uses {@link Class#isAssignableFrom} so
 * handlers match subclasses of the registered type.</p>
 */
public final class EventBus {

    private static final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> HANDLERS =
            new ConcurrentHashMap<>();

    private EventBus() {
    }

    public static <E extends DomainEvent> void subscribe(Class<E> eventType, EventHandler<E> handler) {
        HANDLERS.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(handler);
    }

    public static <E extends DomainEvent> boolean unsubscribe(Class<E> eventType, EventHandler<E> handler) {
        List<EventHandler<? extends DomainEvent>> typedHandlers = HANDLERS.get(eventType);
        return typedHandlers != null && typedHandlers.remove(handler);
    }

    public static void publish(DomainEvent event) {
        for (EventHandler<DomainEvent> handler : handlersFor(event)) {
            handler.handle(event);
        }
    }

    public static Object publishSync(DomainEvent event) {
        Object result = null;
        for (EventHandler<DomainEvent> handler : handlersFor(event)) {
            result = handler.handle(event);
        }
        return result;
    }

    public static void clear() {
        HANDLERS.clear();
    }

    @SuppressWarnings("unchecked")
    private static List<EventHandler<DomainEvent>> handlersFor(DomainEvent event) {
        List<EventHandler<DomainEvent>> matched = new ArrayList<>();
        HANDLERS.forEach((type, typedHandlers) -> {
            if (type.isAssignableFrom(event.getClass())) {
                typedHandlers.forEach(handler -> matched.add((EventHandler<DomainEvent>) handler));
            }
        });
        return matched;
    }
}
