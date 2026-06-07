package com.innospots.nexus.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA/MyBatis-Plus entity for a conversation (chat session).
 * <p>Maps to the {@code nexus_conversation} table and inherits audit
 * fields from {@link ProjectBaseEntity}.</p>
 */
@Getter
@Setter
@Entity
@Table(name = ConversationEntity.TABLE_NAME)
@TableName(ConversationEntity.TABLE_NAME)
public class ConversationEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nexus_conversation";

    @TableId(type = IdType.INPUT)
    @Id
    @Column(length = 64, nullable = false)
    private String conversationId;
    @Column(length = 64)
    private String userId;
    @Column(length = 128)
    private String userName;
    @Column(length = 256)
    private String title;
    @Column(length = 128)
    private String appKey;
    @Column(length = 128)
    private String apiKey;
    @Column(length = 128)
    private String extensionKey;
    @Column(length = 1024)
    private String description;
    @Column(length = 512)
    private String tags;
    @Column(length = 4096)
    private String shareMemory;
}
