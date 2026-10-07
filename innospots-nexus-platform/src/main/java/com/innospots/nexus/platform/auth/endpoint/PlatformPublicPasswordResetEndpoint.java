package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 运营管理平台匿名密码重置 REST。
 *
 * @author Smars
 * @date 2026/10/06
 * @see PlatformConstant#PUBLIC_AUTH_PASSWORD_PATH
 * @see PlatformPasswordService
 */
@Path(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicPasswordReset", description = "运营管理平台密码重置")
@RequiredArgsConstructor
public class PlatformPublicPasswordResetEndpoint {

    private final PlatformPasswordService platformPasswordService;

    @POST
    @Path("/reset")
    @Operation(operationId = "platformAuthPasswordReset", summary = "凭验证码重置密码")
    public R<Void> resetPassword(PasswordResetRequest request) {
        platformPasswordService.resetPassword(request);
        return R.ok();
    }
}
