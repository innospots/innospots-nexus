package com.innospots.nexus.core.plugin.event;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.resource.ResourceScope;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 实例本地同步事件总线，隔离观察者失败。
 */
public final class DefaultPluginEventBus implements PluginEventBus {

    private final CopyOnWriteArrayList<HandlerRegistration<?>> handlers = new CopyOnWriteArrayList<>();
    private final System.Logger logger = System.getLogger(DefaultPluginEventBus.class.getName());
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * 注册一个同步观察者，直到其订阅或所属资源作用域关闭。
     *
     * @param eventType Handler 接受的事件类型
     * @param handler 在发布线程执行的观察者
     * @param <E> 事件类型
     * @return 可幂等取消的订阅句柄
     * @throws NexusException 总线已关闭或输入缺失时抛出
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
     * 向稳定的观察者快照发布事件，并隔离观察者失败。
     *
     * @param event 待发布事件；{@code null} 将被忽略
     */
    @Override
    public void publish(PluginEvent event) {
        if (event == null || closed.get()) {
            return;
        }
        // CopyOnWriteArrayList 的遍历本身就是稳定快照，且不会持有生命周期锁。
        for (HandlerRegistration<?> registration : handlers) {
            try {
                registration.accept(event);
            } catch (RuntimeException exception) {
                logger.log(System.Logger.Level.WARNING, "Plugin event observer failed", exception);
            }
        }
    }

    /** 关闭当前运行时本地总线并释放全部订阅引用。 */
    public void close() {
        // compareAndSet 保证并发 close 只执行一次清理。
        if (closed.compareAndSet(false, true)) {
            handlers.clear();
        }
    }

    /**
     * 返回一个视图，其创建的订阅自动归属于指定插件资源作用域。
     *
     * @param scope 拥有该视图创建的全部订阅的资源作用域
     * @return 作用域事件总线视图
     * @throws NexusException 资源作用域为 {@code null} 时抛出
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
                    // 订阅已对总线可见；如果无法建立作用域所有权，则撤销该订阅。
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
