package com.innospots.nexus.sample.platform.inbound.support.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.sample.platform.core.support.domain.request.SupportTicketOpenRequest;
import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;

/**
 * 对外支持工单 API。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Path("/platform/api/sample/support-tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface InboundSupportTicketEndpoint {

    /**
     * 租户自助开通工单。
     *
     * @param request 开通数据
     * @return 工单概要
     */
    @POST
    R<SupportTicketVo> open(SupportTicketOpenRequest request);
}
