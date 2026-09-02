package com.innospots.nexus.core.plugin.event;

import java.util.function.Consumer;

/**
 * 限定在一个插件管理器内的同步尽力而为事件通道。
 *
 * <p>观察者在发布线程同步执行；单个观察者失败不会中断其他观察者。</p>
 */
public interface PluginEventBus {

    /**
     * 注册类型化事件观察者。
     *
     * @param eventType Handler 接受的事件类型
     * @param handler 在发布线程执行的观察者
     * @param <E> 事件类型
     * @return 可幂等取消的订阅句柄
     */
    <E extends PluginEvent> Subscription subscribe(Class<E> eventType, Consumer<E> handler);

    /**
     * 发布事件观测，不向调用方传播观察者失败。
     *
     * @param event 待发布事件；{@code null} 将被忽略
     */
    void publish(PluginEvent event);
}
