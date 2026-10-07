package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;
import com.innospots.nexus.platform.auth.service.PlatformAuthService;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 运营管理平台已登录会话 REST（刷新、登出、改密）。
 *
 * <p>{@code refresh} 无 Bearer 要求，凭请求体 refresh token；{@code logout} 与改密需 Bearer。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see PlatformConstant#AUTH_PATH
 * @see PlatformAuthService
 * @see PlatformPasswordService
 */
@Path(PlatformConstant.AUTH_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAuthSession", description = "运营管理平台会话")
@RequiredArgsConstructor
public class PlatformAuthSessionEndpoint {

    private final PlatformAuthService platformAuthService;
    private final PlatformPasswordService platformPasswordService;

    @POST
    @Path("/refresh")
    @Operation(operationId = "platformAuthRefresh", summary = "刷新访问令牌")
    public R<AuthTokenVo> refresh(TokenRefreshRequest request) {
        return R.ok(platformAuthService.refresh(request));
    }

    @POST
    @Path("/logout")
    @Operation(operationId = "platformAuthLogout", summary = "登出当前会话")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    public R<Void> logout() {
        platformAuthService.logout();
        return R.ok();
    }

    @POST
    @Path("/password/change")
    @Operation(operationId = "platformAuthPasswordChange", summary = "修改当前用户密码")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    public R<Void> changePassword(PasswordChangeRequest request) {
        platformPasswordService.changePassword(request);
        return R.ok();
    }
}
