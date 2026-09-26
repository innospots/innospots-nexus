package com.innospots.nexus.sample.platform.core.support.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.support.domain.entity.SupportTicketEntity;
import com.innospots.nexus.sample.platform.core.support.domain.request.SupportTicketOpenRequest;
import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;
import com.innospots.nexus.sample.platform.core.support.operator.SupportTicketOperator;

/**
 * 支持工单领域编排。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketOperator supportTicketOperator;

    /**
     * 为租户开通工单。
     *
     * @param request 开通数据
     * @return 工单概要
     */
    public SupportTicketVo open(SupportTicketOpenRequest request) {
        var entity = supportTicketOperator.open(request.tenantId(), request.subject());
        return toVo(entity);
    }

    private static SupportTicketVo toVo(SupportTicketEntity entity) {
        return new SupportTicketVo(
                entity.getTicketId(),
                entity.getTenantId(),
                entity.getSubject(),
                entity.getStatus(),
                entity.getAssigneeUserId());
    }
}
