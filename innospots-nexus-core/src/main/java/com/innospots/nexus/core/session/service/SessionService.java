package com.innospots.nexus.core.session.service;

import com.innospots.nexus.base.events.EventBus;
import com.innospots.nexus.core.session.domain.event.ConversationCreatedEvent;
import com.innospots.nexus.core.session.domain.event.SessionMessageCreatedEvent;
import com.innospots.nexus.core.session.domain.model.Conversation;
import com.innospots.nexus.core.session.domain.model.SessionMessage;
import com.innospots.nexus.core.session.repository.ConversationRepository;
import com.innospots.nexus.core.session.repository.SessionMessageRepository;

/**
 * Application service for session management.
 * <p>Coordinates between {@link ConversationRepository} and
 * {@link SessionMessageRepository}, publishing domain events after
 * each write operation via {@link com.innospots.nexus.base.events.EventBus}.</p>
 */
public class SessionService {

    private final ConversationRepository conversationRepository;
    private final SessionMessageRepository sessionMessageRepository;

    public SessionService(
            ConversationRepository conversationRepository,
            SessionMessageRepository sessionMessageRepository
    ) {
        this.conversationRepository = conversationRepository;
        this.sessionMessageRepository = sessionMessageRepository;
    }

    /**
     * Creates a conversation and publishes a {@link ConversationCreatedEvent}.
     *
     * @return the persisted conversation
     */
    public Conversation createConversation(Conversation conversation) {
        Conversation saved = conversationRepository.save(conversation);
        publish(new ConversationCreatedEvent(saved));
        return saved;
    }

    /**
     * Saves a session message and publishes a {@link SessionMessageCreatedEvent}.
     *
     * @return the persisted message
     */
    public SessionMessage saveMessage(SessionMessage sessionMessage) {
        SessionMessage saved = sessionMessageRepository.save(sessionMessage);
        publish(new SessionMessageCreatedEvent(saved));
        return saved;
    }

    private void publish(com.innospots.nexus.base.events.DomainEvent event) {
        EventBus.publish(event);
    }
}
