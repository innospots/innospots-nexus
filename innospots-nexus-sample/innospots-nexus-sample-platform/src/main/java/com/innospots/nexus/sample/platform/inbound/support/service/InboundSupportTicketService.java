package com.innospots.nexus.sample.platform.inbound.support.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.sample.platform.core.support.domain.request.SupportTicketOpenRequest;
import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;
import com.innospots.nexus.sample.platform.core.support.service.SupportTicketService;

/**
 * 对外支持工单 API 编排（租户自助提单等）。
 *
 * @author Smars
 * @date 2026/09/26
 */
@RequiredArgsConstructor
public class InboundSupportTicketService {

    private final SupportTicketService supportTicketService;

    /**
     * 租户侧开通工单。
     *
     * @param request 开通数据
     * @return 工单概要
     */
    public SupportTicketVo open(SupportTicketOpenRequest request) {
        return supportTicketService.open(request);
    }
}
