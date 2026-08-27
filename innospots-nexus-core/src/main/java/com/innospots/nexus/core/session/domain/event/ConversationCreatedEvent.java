package com.innospots.nexus.core.session.domain.event;

import com.innospots.nexus.base.events.DomainEvent;
import com.innospots.nexus.core.session.domain.model.Conversation;

/** Domain event published when a new {@link Conversation} is created. */
public record ConversationCreatedEvent(Conversation conversation) implements DomainEvent {

    @Override
    public String eventType() {
        return "session.conversation.created";
    }
}
