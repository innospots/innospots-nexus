package com.innospots.nexus.platform.support.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.support.domain.request.SupportAccessGrantCreateRequest;
import com.innospots.nexus.platform.support.domain.vo.SupportAccessGrantVo;

/**
 * 时间限定租户支持访问的运维域契约。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path("/platform/support-access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SupportAccessGrantEndpoint {

    /**
     * 创建待审批的支持访问授权。
     *
     * @param request 授权创建数据
     * @return created grant 概要
     */
    @POST
    R<SupportAccessGrantVo> createGrant(SupportAccessGrantCreateRequest request);

    /**
     * 返回单个支持访问授权。
     *
     * @param grantId grant 标识符
     * @return grant 概要
     */
    @GET
    @Path("/{grantId}")
    R<SupportAccessGrantVo> getGrant(@PathParam("grantId") String grantId);
}
