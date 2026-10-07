package com.innospots.nexus.sample.platform.core.support.operator;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.sample.platform.core.support.dao.SupportTicketDao;
import com.innospots.nexus.sample.platform.core.support.domain.entity.SupportTicketEntity;
import com.innospots.nexus.sample.platform.core.support.domain.enums.SupportTicketStatus;

/**
 * 支持工单单表操作。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class SupportTicketOperator {

    private final SupportTicketDao supportTicketDao;

    /**
     * 创建开放工单。
     *
     * @param tenantId 租户标识
     * @param subject 主题
     * @return persisted 工单
     */
    public SupportTicketEntity open(String tenantId, String subject) {
        requireText(tenantId, "tenantId");
        requireText(subject, "subject");
        SupportTicketEntity entity = new SupportTicketEntity();
        entity.setTenantId(tenantId);
        entity.setSubject(subject);
        entity.setStatus(SupportTicketStatus.OPEN);
        supportTicketDao.insert(entity);
        return entity;
    }

    /**
     * 分派处理人并进入处理中状态。
     *
     * @param ticketId 工单标识
     * @param assigneeUserId 处理人
     * @return updated 工单
     */
    public SupportTicketEntity assign(String ticketId, String assigneeUserId) {
        requireText(assigneeUserId, "assigneeUserId");
        SupportTicketEntity entity = supportTicketDao.selectById(ticketId);
        if (entity == null) {
            throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND, "support ticket not found");
        }
        entity.setAssigneeUserId(assigneeUserId);
        entity.setStatus(SupportTicketStatus.IN_PROGRESS);
        supportTicketDao.updateById(entity);
        return entity;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, field + " is required");
        }
    }
}
