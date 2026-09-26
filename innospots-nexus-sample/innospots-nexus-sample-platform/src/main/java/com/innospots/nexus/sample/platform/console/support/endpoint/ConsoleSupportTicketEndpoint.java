package com.innospots.nexus.sample.platform.console.support.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.sample.platform.core.support.domain.request.SupportTicketOpenRequest;
import com.innospots.nexus.sample.platform.core.support.domain.vo.SupportTicketVo;

/**
 * 管理台支持工单 Jakarta REST 契约。
 *
 * @author Smars
 * @date 2026/09/26
 */
@Path("/platform/console/sample/support-tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ConsoleSupportTicketEndpoint {

    /**
     * 开通工单。
     *
     * @param request 开通数据
     * @return 工单概要
     */
    @POST
    R<SupportTicketVo> open(SupportTicketOpenRequest request);

    /**
     * 分派工单。
     *
     * @param ticketId 工单标识
     * @param assigneeUserId 处理人用户标识
     * @return 工单概要
     */
    @POST
    @Path("/{ticketId}/assign/{assigneeUserId}")
    R<SupportTicketVo> assign(
            @PathParam("ticketId") String ticketId,
            @PathParam("assigneeUserId") String assigneeUserId);
}
