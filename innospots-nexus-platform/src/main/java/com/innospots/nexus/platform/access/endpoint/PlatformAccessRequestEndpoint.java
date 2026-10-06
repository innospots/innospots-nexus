package com.innospots.nexus.platform.access.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestApproveRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestPageRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestRejectRequest;
import com.innospots.nexus.platform.access.domain.vo.PlatformAccessRequestVo;
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

/**
 * 注册待审核管理端 REST：分页、通过（用户 {@code ACTIVE}）、拒绝。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.service.PlatformAccessRequestService
 */
@Path(PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAccessRegistration", description = "主动注册审批")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformAccessRequestEndpoint {

    private final PlatformAccessRequestService accessRequestService;

    @GET
    @Operation(operationId = "platformAccessRequestPage", summary = "分页查询访问申请")
    public R<PageResult<PlatformAccessRequestVo>> pageAccessRequests(
            @BeanParam PlatformAccessRequestPageRequest request) {
        return R.ok(accessRequestService.pageAccessRequests(request));
    }

    @GET
    @Path("/{accessRequestId}")
    @Operation(operationId = "platformAccessRequestGet", summary = "查询访问申请")
    public R<PlatformAccessRequestVo> getAccessRequest(
            @Parameter(description = "申请 ID", required = true) @PathParam("accessRequestId") String accessRequestId) {
        return R.ok(accessRequestService.getAccessRequest(accessRequestId));
    }

    @POST
    @Path("/{accessRequestId}/approve")
    @Operation(operationId = "platformAccessRequestApprove", summary = "通过注册待审核申请")
    public R<PlatformUserVo> approveAccessRequest(
            @PathParam("accessRequestId") String accessRequestId,
            PlatformAccessRequestApproveRequest request) {
        return R.ok(accessRequestService.approveAccessRequest(accessRequestId, request));
    }

    @POST
    @Path("/{accessRequestId}/reject")
    @Operation(operationId = "platformAccessRequestReject", summary = "拒绝访问申请")
    public R<Void> rejectAccessRequest(
            @PathParam("accessRequestId") String accessRequestId,
            PlatformAccessRequestRejectRequest request) {
        accessRequestService.rejectAccessRequest(accessRequestId, request);
        return R.ok();
    }
}
