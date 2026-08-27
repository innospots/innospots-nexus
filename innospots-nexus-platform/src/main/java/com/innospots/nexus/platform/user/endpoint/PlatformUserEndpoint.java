package com.innospots.nexus.platform.user.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

/**
 * Ops-domain contract for platform user administration.
 * <p>Public self-registration is not exposed. Accounts are created by
 * administrators only.</p>
 */
@Path("/platform/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PlatformUserEndpoint {

    /**
     * Creates a platform user with a local password.
     *
     * @param request admin create request
     * @return created user summary
     */
    @POST
    R<PlatformUserVo> createUser(PlatformUserCreateRequest request);

    /**
     * Returns one platform user.
     *
     * @param platformUserId platform-realm user identifier
     * @return user summary
     */
    @GET
    @Path("/{platformUserId}")
    R<PlatformUserVo> getUser(@PathParam("platformUserId") String platformUserId);
}
