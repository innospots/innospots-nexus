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
 * Persistence entity for a conversation (chat session).
 */
@Getter
@Setter
@Entity
@Table(name = ConversationEntity.TABLE_NAME, indexes = {
        @Index(name = "idx_nx_conversation_user", columnList = "workspace_id,user_id"),
        @Index(name = "idx_nx_conversation_app", columnList = "app_key")
})
@TableName(ConversationEntity.TABLE_NAME)
public class ConversationEntity extends WorkspaceBaseEntity {

    public static final String TABLE_NAME = "nx_conversation";

    /**
     * Conversation identifier.
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String conversationId;

    @Override
    public String idPrefix() {
        return "cnv";
    }

    /**
     * Owner user identifier.
     */
    @Column(length = 32)
    private String userId;

    /**
     * Owner user name.
     */
    @Column(length = 128)
    private String userName;

    /**
     * Display title.
     */
    @Column(length = 256)
    private String title;

    /**
     * Application key that owns this conversation.
     */
    @Column(length = 128)
    private String appKey;

    /**
     * API key used when the conversation was opened.
     */
    @Column(length = 128)
    private String apiKey;

    /**
     * Extension key associated with this conversation.
     */
    @Column(length = 128)
    private String extensionKey;

    /**
     * Optional description.
     */
    @Column(length = 1024)
    private String description;

    /**
     * Serialized tag list.
     */
    @Column(length = 512)
    private String tags;

    /**
     * Serialized shared memory payload.
     */
    @Column(length = 4096)
    private String sharedMemory;
}
