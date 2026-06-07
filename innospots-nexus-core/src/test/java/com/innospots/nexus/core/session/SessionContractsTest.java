package com.innospots.nexus.core.session;

import com.innospots.nexus.base.domain.field.DomainField;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.events.DomainEvent;
import com.innospots.nexus.base.events.EventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SessionContractsTest {

    @BeforeEach
    @AfterEach
    void clearEventBus() {
        EventBus.clear();
    }

    @Test
    void createsConversationWithImmutableTagsAndMemory() {
        Conversation conversation = Conversation.named("conv-1", "AI Assistant", "app")
                .apiKey("api")
                .extensionKey("ext")
                .user("u-1", "Ada")
                .description("help session")
                .tags(List.of("ai", "ops"))
                .shareMemory(Map.of("locale", "zh-CN"));

        assertThat(conversation.conversationId()).isEqualTo("conv-1");
        assertThat(conversation.userId()).isEqualTo("u-1");
        assertThat(conversation.tags()).containsExactly("ai", "ops");
        assertThat(conversation.shareMemory()).containsEntry("locale", "zh-CN");
        assertThat(conversation.createdTime()).isNotNull();
        assertThat(conversation.updatedTime()).isNotNull();
    }

    @Test
    void createsSessionMessageWithDisplayStructure() {
        SessionMessage message = SessionMessage.of("conv-1", "msg-1", SessionMessageType.MARKDOWN)
                .sessionId("session-1")
                .user("u-1", "Ada")
                .title("answer")
                .description("markdown response")
                .payload(Map.of("content", "hello"))
                .fields(List.of(DomainField.named("Name", "name", "String")))
                .downloadUrl("https://example.test/download")
                .previewUrl("https://example.test/preview")
                .slice(true);

        assertThat(message.conversationId()).isEqualTo("conv-1");
        assertThat(message.messageId()).isEqualTo("msg-1");
        assertThat(message.messageType()).isEqualTo(SessionMessageType.MARKDOWN);
        assertThat(message.fields()).hasSize(1);
        assertThat(message.createdTime()).isNotNull();
        assertThat(message.slice()).isTrue();
    }

    @Test
    void resolvesMessageTypeFromMimeType() {
        assertThat(SessionMessageType.fromMimeType("image/png")).isEqualTo(SessionMessageType.IMAGE);
        assertThat(SessionMessageType.fromMimeType("video/mp4")).isEqualTo(SessionMessageType.VIDEO);
        assertThat(SessionMessageType.fromMimeType("application/pdf")).isEqualTo(SessionMessageType.FILE);
        assertThat(SessionMessageType.fromMimeType("text/plain")).isEqualTo(SessionMessageType.FILE);
        assertThat(SessionMessageType.fromMimeType(null)).isEqualTo(SessionMessageType.TEXT);
    }

    @Test
    void savesConversationAndMessageThroughPortsAndPublishesEvents() {
        InMemoryConversationRepository conversationRepository = new InMemoryConversationRepository();
        InMemorySessionMessageRepository messageRepository = new InMemorySessionMessageRepository();
        List<DomainEvent> events = new ArrayList<>();
        EventBus.subscribe(ConversationCreatedEvent.class, event -> {
            events.add(event);
            return null;
        });
        EventBus.subscribe(SessionMessageCreatedEvent.class, event -> {
            events.add(event);
            return null;
        });
        SessionService service = new SessionService(conversationRepository, messageRepository);

        Conversation conversation = service.createConversation(Conversation.named("conv-1", "AI", "app"));
        SessionMessage message = service.saveMessage(SessionMessage.of("conv-1", "msg-1", SessionMessageType.TEXT));

        assertThat(conversationRepository.findById("conv-1")).contains(conversation);
        assertThat(messageRepository.findById("msg-1")).contains(message);
        assertThat(events).extracting(DomainEvent::eventType)
                .containsExactly("session.conversation.created", "session.message.created");
    }

    private static final class InMemoryConversationRepository implements ConversationRepository {

        private final List<Conversation> conversations = new ArrayList<>();

        @Override
        public Conversation save(Conversation conversation) {
            conversations.add(conversation);
            return conversation;
        }

        @Override
        public boolean updateTitle(String conversationId, String title) {
            return false;
        }

        @Override
        public boolean updateTags(String conversationId, List<String> tags) {
            return false;
        }

        @Override
        public boolean updateShareMemory(String conversationId, Map<String, Object> shareMemory) {
            return false;
        }

        @Override
        public boolean deleteById(String conversationId) {
            return conversations.removeIf(conversation -> conversation.conversationId().equals(conversationId));
        }

        @Override
        public Optional<Conversation> findById(String conversationId) {
            return conversations.stream()
                    .filter(conversation -> conversation.conversationId().equals(conversationId))
                    .findFirst();
        }

        @Override
        public List<Conversation> listByApp(String appKey, int maxSize) {
            return List.of();
        }

        @Override
        public List<Conversation> listByScope(String appKey, String apiKey, String extensionKey, int maxSize) {
            return List.of();
        }

        @Override
        public PageResult<Conversation> pageByApp(String appKey, long pageNo, long pageSize) {
            return PageResult.empty(pageNo, pageSize);
        }

        @Override
        public PageResult<Conversation> pageByScope(
                String appKey,
                String apiKey,
                String extensionKey,
                long pageNo,
                long pageSize
        ) {
            return PageResult.empty(pageNo, pageSize);
        }
    }

    private static final class InMemorySessionMessageRepository implements SessionMessageRepository {

        private final List<SessionMessage> messages = new ArrayList<>();

        @Override
        public SessionMessage save(SessionMessage sessionMessage) {
            messages.add(sessionMessage);
            return sessionMessage;
        }

        @Override
        public boolean deleteById(String messageId) {
            return messages.removeIf(message -> message.messageId().equals(messageId));
        }

        @Override
        public Optional<SessionMessage> findById(String messageId) {
            return messages.stream()
                    .filter(message -> message.messageId().equals(messageId))
                    .findFirst();
        }

        @Override
        public List<SessionMessage> listByConversation(String conversationId, int maxSize) {
            return messages.stream()
                    .filter(message -> message.conversationId().equals(conversationId))
                    .limit(maxSize)
                    .toList();
        }

        @Override
        public List<SessionMessage> listLastByApp(String appKey, int maxSize) {
            return List.of();
        }

        @Override
        public PageResult<SessionMessage> pageByConversation(String conversationId, long pageNo, long pageSize) {
            return PageResult.empty(pageNo, pageSize);
        }
    }
}
