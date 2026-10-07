package com.innospots.nexus.base.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 简单的内存事件总线。订阅者通过 {@link #subscribe} 注册特定事件类型；
 * 发布者通过 {@link #publish}（即发即弃）或 {@link #publishSync}（阻塞，返回最后处理器结果）触发事件。
 * <p>处理器存储在 {@link CopyOnWriteArrayList} 中以支持安全的并发迭代。
 * 事件类型匹配使用 {@link Class#isAssignableFrom}，因此处理器可匹配已注册类型的子类。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see DomainEvent
 * @see EventHandler
 */
public final class EventBus {

    private static final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> HANDLERS =
            new ConcurrentHashMap<>();

    private EventBus() {
    }

    /**
     * 订阅指定类型的事件。
     *
     * @param eventType 事件类型
     * @param handler   事件处理器
     * @param <E>       事件类型参数
     */
    public static <E extends DomainEvent> void subscribe(Class<E> eventType, EventHandler<E> handler) {
        HANDLERS.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * 取消订阅指定类型的事件处理器。
     *
     * @param eventType 事件类型
     * @param handler   事件处理器
     * @param <E>       事件类型参数
     * @return 取消成功时返回 {@code true}
     */
    public static <E extends DomainEvent> boolean unsubscribe(Class<E> eventType, EventHandler<E> handler) {
        List<EventHandler<? extends DomainEvent>> typedHandlers = HANDLERS.get(eventType);
        return typedHandlers != null && typedHandlers.remove(handler);
    }

    /**
     * 异步发布事件（即发即弃）。
     *
     * @param event 领域事件
     */
    public static void publish(DomainEvent event) {
        for (EventHandler<DomainEvent> handler : handlersFor(event)) {
            handler.handle(event);
        }
    }

    /**
     * 同步发布事件，阻塞直到所有处理器完成，返回最后一个处理器的结果。
     *
     * @param event 领域事件
     * @return 最后一个处理器的返回值
     */
    public static Object publishSync(DomainEvent event) {
        Object result = null;
        for (EventHandler<DomainEvent> handler : handlersFor(event)) {
            result = handler.handle(event);
        }
        return result;
    }

    /**
     * 清除所有已注册的处理器。
     */
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
