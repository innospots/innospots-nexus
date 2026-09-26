package com.innospots.nexus.sample.platform.console.support.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.support.assignment.service.SupportAssignmentService;
import com.innospots.nexus.sample.platform.core.support.domain.request.SupportTicketOpenRequest;
import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;
import com.innospots.nexus.sample.platform.core.support.service.SupportTicketService;

/**
 * 管理台支持工单编排。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class ConsoleSupportTicketService {

    private final SupportTicketService supportTicketService;
    private final SupportAssignmentService supportAssignmentService;

    /**
     * 开通工单。
     *
     * @param request 开通数据
     * @return 工单概要
     */
    public SupportTicketVo open(SupportTicketOpenRequest request) {
        return supportTicketService.open(request);
    }

    /**
     * 分派工单。
     *
     * @param ticketId 工单标识
     * @param assigneeUserId 处理人
     * @return 工单概要
     */
    public SupportTicketVo assign(String ticketId, String assigneeUserId) {
        return supportAssignmentService.assign(ticketId, assigneeUserId);
    }
}
