package com.innospots.nexus.core.session;

import com.innospots.nexus.base.domain.response.PageResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository port for {@link Conversation} persistence.
 * <p>Implementations provide the actual storage adapter (database, in-memory, etc.).
 * This interface is defined in the domain layer as a port — concrete adapters
 * belong in infrastructure modules.</p>
 */
public interface ConversationRepository {

    Conversation save(Conversation conversation);

    boolean updateTitle(String conversationId, String title);

    boolean updateTags(String conversationId, List<String> tags);

    boolean updateShareMemory(String conversationId, Map<String, Object> shareMemory);

    boolean deleteById(String conversationId);

    Optional<Conversation> findById(String conversationId);

    List<Conversation> listByApp(String appKey, int maxSize);

    List<Conversation> listByScope(String appKey, String apiKey, String extensionKey, int maxSize);

    PageResult<Conversation> pageByApp(String appKey, long pageNo, long pageSize);

    PageResult<Conversation> pageByScope(
            String appKey,
            String apiKey,
            String extensionKey,
            long pageNo,
            long pageSize
    );
}
