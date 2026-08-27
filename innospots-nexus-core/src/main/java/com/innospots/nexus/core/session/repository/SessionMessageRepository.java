package com.innospots.nexus.core.session.repository;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.core.session.domain.model.SessionMessage;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for {@link SessionMessage} persistence.
 * <p>Defined as a domain port — concrete implementations belong in
 * infrastructure modules.</p>
 */
public interface SessionMessageRepository {

    SessionMessage save(SessionMessage sessionMessage);

    boolean deleteById(String messageId);

    Optional<SessionMessage> findById(String messageId);

    List<SessionMessage> listByConversation(String conversationId, int maxSize);

    List<SessionMessage> listLastByApp(String appKey, int maxSize);

    PageResult<SessionMessage> pageByConversation(String conversationId, long pageNo, long pageSize);
}
