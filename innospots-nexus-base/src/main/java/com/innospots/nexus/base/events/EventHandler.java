package com.innospots.nexus.base.events;

/**
 * 领域事件处理器的函数式接口。
 *
 * @author Smars
 * @date 2026/09/13
 * @param <E> 此处理器处理的具体事件类型
 * @see DomainEvent
 * @see EventBus
 */
@FunctionalInterface
public interface EventHandler<E extends DomainEvent> {

    /**
     * 处理领域事件。
     *
     * @param event 领域事件
     * @return 处理结果（可为 null）
     */
    Object handle(E event);
}
