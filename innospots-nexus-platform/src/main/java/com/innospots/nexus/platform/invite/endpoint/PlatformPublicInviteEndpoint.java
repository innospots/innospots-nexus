package com.innospots.nexus.platform.invite.endpoint;

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

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteAcceptRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteActivateByCodeRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteOtpRequest;
import com.innospots.nexus.platform.invite.domain.vo.PlatformInvitePreviewVo;
import com.innospots.nexus.platform.invite.service.PlatformPublicInviteService;

/**
 * 邀请注册公开 REST（{@link com.innospots.nexus.platform.config.PlatformConstant#PUBLIC_INVITES_PATH}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.service.PlatformPublicInviteService
 */
@EndpointModule(value = "user", ignoreAuth = true)
@Path(PlatformConstant.PUBLIC_INVITES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicInvite", description = "邀请注册")
@RequiredArgsConstructor
public class PlatformPublicInviteEndpoint {

    private final PlatformPublicInviteService publicInviteService;

    @GET
    @Path("/{token}")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.invite.public.preview",
            label = @I18nLiteral(pairs = {"zh-CN", "预览邀请令牌", "en", "Preview invite token"}),
            ignorePermission = true)
    @Operation(operationId = "platformPublicInvitePreview", summary = "预览邀请令牌")
    public R<PlatformInvitePreviewVo> previewInvite(
            @Parameter(description = "邀请令牌", required = true) @PathParam("token") String token) {
        return R.ok(publicInviteService.previewInvite(token));
    }

    @POST
    @Path("/{token}/otp")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "platform.invite.public.otp",
            label = @I18nLiteral(pairs = {"zh-CN", "邀请注册页获取验证码", "en", "Issue invite accept OTP"}),
            recordArgs = false,
            ignorePermission = true)
    @Operation(operationId = "platformPublicInviteAcceptOtp", summary = "邀请注册页获取验证码")
    public R<Void> issueInviteAcceptOtp(
            @PathParam("token") String token,
            PlatformInviteOtpRequest request) {
        publicInviteService.issueInviteAcceptOtp(token, request);
        return R.ok();
    }

    @POST
    @Path("/{token}/accept")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "platform.invite.public.accept",
            label = @I18nLiteral(pairs = {"zh-CN", "通过邀请链接注册", "en", "Accept invite"}),
            recordArgs = false,
            auditLog = true,
            ignorePermission = true)
    @Operation(operationId = "platformPublicInviteAccept", summary = "通过邀请链接注册")
    public R<String> acceptInvite(
            @PathParam("token") String token,
            PlatformInviteAcceptRequest request) {
        return R.ok(publicInviteService.acceptInvite(token, request));
    }

    @POST
    @Path("/activate-by-code")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "platform.invite.public.activate-by-code",
            label = @I18nLiteral(pairs = {"zh-CN", "凭邀请码注册", "en", "Activate by invite code"}),
            recordArgs = false,
            auditLog = true,
            ignorePermission = true)
    @Operation(operationId = "platformPublicInviteActivateByCode", summary = "凭邀请码注册")
    public R<String> activateByInviteCode(PlatformInviteActivateByCodeRequest request) {
        return R.ok(publicInviteService.activateByInviteCode(request));
    }
}
