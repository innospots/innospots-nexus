package com.innospots.nexus.core.session.domain.event;

import com.innospots.nexus.base.events.DomainEvent;
import com.innospots.nexus.core.session.domain.model.SessionMessage;

/** Domain event published when a new {@link SessionMessage} is created. */
public record SessionMessageCreatedEvent(SessionMessage sessionMessage) implements DomainEvent {

    @Override
    public String eventType() {
        return "session.message.created";
    }
}
