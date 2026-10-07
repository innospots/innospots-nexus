package com.innospots.nexus.sample.platform.core.support.domain.vo;

import com.innospots.nexus.sample.platform.core.support.domain.enums.SupportTicketStatus;

/**
 * 支持工单概要。
 *
 * @param ticketId 工单标识
 * @param tenantId 租户标识
 * @param subject 主题
 * @param status 状态
 * @param assigneeUserId 处理人（可空）
 * @author Smars
 * @date 2026/09/26
 */
public record SupportTicketVo(
        String ticketId,
        String tenantId,
        String subject,
        SupportTicketStatus status,
        String assigneeUserId) {
}
