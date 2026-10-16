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

/**
 * 匿名密码重置 REST 契约。
 *
 * <p>类级 {@code @Path} 由业务域实现（如 platform）绑定。</p>
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PublicPasswordReset", description = "匿名密码重置")
public interface PublicPasswordResetEndpoint {

    @POST
    @Path("/reset")
    @Operation(operationId = "authPasswordReset", summary = "凭验证码重置密码")
    R<Void> resetPassword(PasswordResetRequest request);
}
