package com.innospots.nexus.sample.platform.core.support.assignment.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;
import com.innospots.nexus.sample.platform.core.support.operator.SupportTicketOperator;

/**
 * 支持工单子能力：分派编排（功能子模块示例）。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class SupportAssignmentService {

    private final SupportTicketOperator supportTicketOperator;

    /**
     * 将工单分派给平台用户。
     *
     * @param ticketId 工单标识
     * @param assigneeUserId 处理人
     * @return 工单概要
     */
    public SupportTicketVo assign(String ticketId, String assigneeUserId) {
        var entity = supportTicketOperator.assign(ticketId, assigneeUserId);
        return new SupportTicketVo(
                entity.getTicketId(),
                entity.getTenantId(),
                entity.getSubject(),
                entity.getStatus(),
                entity.getAssigneeUserId());
    }
}
