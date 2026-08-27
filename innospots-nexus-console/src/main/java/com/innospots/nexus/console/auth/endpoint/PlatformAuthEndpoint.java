package com.innospots.nexus.console.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * Ops-domain authentication. There is no public register on this path.
 */
@Path("/platform/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PlatformAuthEndpoint {

    /**
     * Logs in a platform user.
     *
     * @param request login identity and encrypted password
     * @return PLATFORM token
     */
    @POST
    @Path("/login")
    R<AuthTokenVo> login(AuthLoginRequest request);

    /**
     * Refreshes a PLATFORM token pair.
     *
     * @param request refresh token
     * @return new PLATFORM token
     */
    @POST
    @Path("/refresh")
    R<AuthTokenVo> refresh(TokenRefreshRequest request);

    /**
     * Revokes the current PLATFORM refresh token.
     *
     * @return empty success
     */
    @POST
    @Path("/logout")
    R<Void> logout();

    /**
     * Changes password for the authenticated platform user.
     *
     * @param request old and new encrypted passwords
     * @return empty success
     */
    @POST
    @Path("/password/change")
    R<Void> changePassword(PasswordChangeRequest request);

    /**
     * Resets a platform user password with a verification code.
     *
     * @param request identity, code, and new password
     * @return empty success
     */
    @POST
    @Path("/password/reset")
    R<Void> resetPassword(PasswordResetRequest request);
}
