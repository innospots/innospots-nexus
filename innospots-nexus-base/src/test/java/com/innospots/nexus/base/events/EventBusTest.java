package com.innospots.nexus.base.events;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class EventBusTest {

    @BeforeEach
    @AfterEach
    void clearEventBus() {
        EventBus.clear();
    }

    @Test
    void publishesSynchronousEventsAndReturnsHandlerResult() {
        EventBus.subscribe(UserCreatedEvent.class, event -> event.userName().toUpperCase());

        Object result = EventBus.publishSync(new UserCreatedEvent("alice"));

        assertThat(result).isEqualTo("ALICE");
    }

    @Test
    void publishesEventsToRegisteredHandlers() {
        AtomicReference<String> received = new AtomicReference<>();
        EventBus.subscribe(UserCreatedEvent.class, event -> {
            received.set(event.userName());
            return null;
        });

        EventBus.publish(new UserCreatedEvent("bob"));

        assertThat(received).hasValue("bob");
    }

    record UserCreatedEvent(String userName) implements DomainEvent {

        @Override
        public String eventType() {
            return "user.created";
        }
    }
}
