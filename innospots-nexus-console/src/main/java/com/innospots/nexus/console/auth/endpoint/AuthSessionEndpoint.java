package com.innospots.nexus.console.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;

/**
 * 已登录会话 REST 契约（刷新、登出、改密）。
 *
 * <p>类级 {@code @Path} 由业务域实现（如 platform）绑定。</p>
 */
@EndpointModule("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "AuthSession", description = "认证会话")
public interface AuthSessionEndpoint {

    @POST
    @Path("/refresh")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "auth.token.refresh",
            label = @I18nLiteral(pairs = {"zh-CN", "刷新访问令牌", "en", "Refresh access token"}),
            recordArgs = false,
            ignoreAuth = true)
    @Operation(operationId = "authRefresh", summary = "刷新访问令牌")
    R<AuthTokenVo> refresh(TokenRefreshRequest request);

    @POST
    @Path("/logout")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "auth.logout",
            label = @I18nLiteral(pairs = {"zh-CN", "登出当前会话", "en", "Logout"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "authLogout", summary = "登出当前会话")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    R<Void> logout();

    @POST
    @Path("/password/change")
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "auth.password.change",
            label = @I18nLiteral(pairs = {"zh-CN", "修改当前用户密码", "en", "Change password"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "authPasswordChange", summary = "修改当前用户密码")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    R<Void> changePassword(PasswordChangeRequest request);
}
