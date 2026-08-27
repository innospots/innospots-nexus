package com.innospots.nexus.core.session.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.WorkspaceBaseEntity;

/**
 * Persistence entity for a single message within a conversation.
 */
@Getter
@Setter
@Entity
@Table(name = SessionMessageEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_session_message_conversation", columnList = "conversation_id"),
        @Index(name = "idx_nx_session_message_session", columnList = "session_id")
})
@TableName(SessionMessageEntity.TABLE_NAME)
public class SessionMessageEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_session_message";

    /**
     * Message identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String messageId;

    @Override
    public String idPrefix() {
        return "msg";
    }

    /**
     * Session / thread identifier.
     */
    @Column(length = 32)
    private String sessionId;

    /**
     * Parent conversation identifier.
     */
    @Column(length = 32)
    private String conversationId;

    /**
     * Message type code.
     */
    @Column(length = 32)
    private String messageType;

    /**
     * Message body.
     */
    @Column(length = 4096)
    private String body;
}
