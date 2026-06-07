package com.innospots.nexus.core.session;

import com.innospots.nexus.base.events.DomainEvent;

/** Domain event published when a new {@link SessionMessage} is created. */
public record SessionMessageCreatedEvent(SessionMessage sessionMessage) implements DomainEvent {

    @Override
    public String eventType() {
        return "session.message.created";
    }
}
