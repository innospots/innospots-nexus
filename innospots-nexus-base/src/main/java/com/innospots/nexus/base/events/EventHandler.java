package com.innospots.nexus.base.events;

/**
 * Functional interface for domain event handlers.
 *
 * @param <E> the concrete event type this handler processes
 */
@FunctionalInterface
public interface EventHandler<E extends DomainEvent> {

    Object handle(E event);
}
