package com.innospots.nexus.sample.platform.core.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.core.persistence.entity.BaseEntity;
import com.innospots.nexus.sample.platform.core.support.domain.enums.SupportTicketStatus;

/**
 * 平台支持工单。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Getter
@Setter
@Entity
@Table(name = SupportTicketEntity.TABLE_NAME)
@TableName(SupportTicketEntity.TABLE_NAME)
public class SupportTicketEntity extends BaseEntity {

    public static final String TABLE_NAME = "nx_sample_support_ticket";

    @TableId(type = IdType.ASSIGN_UUID)
    @Id
    @Column(length = 32, nullable = false)
    private String ticketId;

    @Column(length = 32, nullable = false)
    private String tenantId;

    @Column(length = 256, nullable = false)
    private String subject;

    @Column(length = 16, nullable = false)
    private SupportTicketStatus status;

    @Column(length = 32)
    private String assigneeUserId;
}
