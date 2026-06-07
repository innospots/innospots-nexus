package com.innospots.nexus.base.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events. Each event has a unique ID,
 * a type identifier, and a timestamp of occurrence.
 */
public interface DomainEvent {

    default String eventId() {
        return UUID.randomUUID().toString();
    }

    String eventType();

    default Instant occurredAt() {
        return Instant.now();
    }
}
