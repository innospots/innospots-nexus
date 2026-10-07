package com.innospots.nexus.platform.invite.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteCreateRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInvitePageRequest;
import com.innospots.nexus.platform.invite.domain.vo.PlatformInviteVo;
import com.innospots.nexus.platform.invite.service.PlatformInviteService;

/**
 * 平台用户邀请管理端 API（发邀请、重发、撤销）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.service.PlatformInviteService
 */
@Path(PlatformConstant.INVITES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformInvite", description = "平台用户邀请")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformInviteEndpoint {

    private final PlatformInviteService inviteService;

    @GET
    @Operation(operationId = "platformInvitePage", summary = "分页查询邀请")
    public R<PageResult<PlatformInviteVo>> pageInvites(@BeanParam PlatformInvitePageRequest request) {
        return R.ok(inviteService.pageInvites(request));
    }

    @POST
    @Operation(operationId = "platformInviteCreate", summary = "新建邀请")
    public R<PlatformInviteVo> createInvite(PlatformInviteCreateRequest request) {
        return R.ok(inviteService.createInvite(request));
    }

    @GET
    @Path("/{inviteId}")
    @Operation(operationId = "platformInviteGet", summary = "查询邀请详情")
    public R<PlatformInviteVo> getInvite(
            @Parameter(description = "邀请 ID", required = true) @PathParam("inviteId") String inviteId) {
        return R.ok(inviteService.getInvite(inviteId));
    }

    @POST
    @Path("/{inviteId}/resend")
    @Operation(operationId = "platformInviteResend", summary = "重发邀请")
    public R<PlatformInviteVo> resendInvite(
            @Parameter(description = "邀请 ID", required = true) @PathParam("inviteId") String inviteId,
            @QueryParam("locale") String locale) {
        return R.ok(inviteService.resendInvite(inviteId, locale));
    }

    @POST
    @Path("/{inviteId}/revoke")
    @Operation(operationId = "platformInviteRevoke", summary = "撤销邀请")
    public R<Void> revokeInvite(
            @Parameter(description = "邀请 ID", required = true) @PathParam("inviteId") String inviteId) {
        inviteService.revokeInvite(inviteId);
        return R.ok();
    }
}
