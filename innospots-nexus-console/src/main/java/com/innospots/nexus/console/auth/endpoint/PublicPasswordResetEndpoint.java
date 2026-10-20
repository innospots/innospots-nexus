package com.innospots.nexus.console.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;

/**
 * 匿名密码重置 REST 契约。
 *
 * <p>类级 {@code @Path} 由业务域实现（如 platform）绑定。</p>
 */
@EndpointModule(value = "auth", ignoreAuth = true)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PublicPasswordReset", description = "匿名密码重置")
public interface PublicPasswordResetEndpoint {

    @POST
    @Path("/reset")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "auth.password.reset",
            label = @I18nLiteral(pairs = {"zh-CN", "凭验证码重置密码", "en", "Reset password with verification"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "authPasswordReset", summary = "凭验证码重置密码")
    R<Void> resetPassword(PasswordResetRequest request);
}
