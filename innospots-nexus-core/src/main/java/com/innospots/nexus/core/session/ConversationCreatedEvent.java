package com.innospots.nexus.core.session;

import com.innospots.nexus.base.events.DomainEvent;

/** Domain event published when a new {@link Conversation} is created. */
public record ConversationCreatedEvent(Conversation conversation) implements DomainEvent {

    @Override
    public String eventType() {
        return "session.conversation.created";
    }
}
