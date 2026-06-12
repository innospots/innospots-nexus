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
 * JPA/MyBatis-Plus entity for a single message within a conversation.
 * <p>Maps to the {@code nexus_session_message} table and inherits audit
 * fields from {@link ProjectBaseEntity}.</p>
 */
@Getter
@Setter
@Entity
@Table(name = SessionMessageEntity.TABLE_NAME)
@TableName(SessionMessageEntity.TABLE_NAME)
public class SessionMessageEntity extends ProjectBaseEntity {

    public static final String TABLE_NAME = "nexus_session_message";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String messageId;

    @Override
    public String idPrefix() {
        return "msg";
    }
    @Column(length = 64)
    private String sessionId;
    @Column(length = 64)
    private String conversationId;
    @Column(length = 32)
    private String messageType;
    @Column(length = 4096)
    private String body;
}
