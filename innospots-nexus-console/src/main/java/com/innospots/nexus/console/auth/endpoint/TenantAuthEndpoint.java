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
import com.innospots.nexus.console.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.console.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * Tenant-realm authentication and identity registration.
 */
@Path("/tenant/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TenantAuthEndpoint {

    /**
     * Registers a tenant-realm login identity without creating a TenantMember.
     *
     * @param request identity and encrypted password
     * @return TENANT identity token
     */
    @POST
    @Path("/register")
    R<AuthTokenVo> register(TenantRegisterRequest request);

    /**
     * Logs in a tenant user.
     *
     * @param request login identity and encrypted password
     * @return identity or business token depending on membership count
     */
    @POST
    @Path("/login")
    R<AuthTokenVo> login(AuthLoginRequest request);

    /**
     * Exchanges an identity token for a business token bound to one tenant.
     *
     * @param request tenant to activate
     * @return TENANT business token
     */
    @POST
    @Path("/select-tenant")
    R<AuthTokenVo> selectTenant(SelectTenantRequest request);

    /**
     * Refreshes a TENANT token pair.
     *
     * @param request refresh token
     * @return new TENANT token
     */
    @POST
    @Path("/refresh")
    R<AuthTokenVo> refresh(TokenRefreshRequest request);

    /**
     * Revokes the current TENANT refresh token.
     *
     * @return empty success
     */
    @POST
    @Path("/logout")
    R<Void> logout();

    /**
     * Changes password for the authenticated tenant user.
     *
     * @param request old and new encrypted passwords
     * @return empty success
     */
    @POST
    @Path("/password/change")
    R<Void> changePassword(PasswordChangeRequest request);

    /**
     * Resets a tenant user password with a verification code.
     *
     * @param request identity, code, and new password
     * @return empty success
     */
    @POST
    @Path("/password/reset")
    R<Void> resetPassword(PasswordResetRequest request);
}
