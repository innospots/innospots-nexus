package com.innospots.nexus.kernel.role.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.role.domain.request.RoleMemberAddRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleMemberPageRequest;
import com.innospots.nexus.kernel.role.domain.vo.RoleMemberVo;

/**
 * Management-console endpoint for users assigned to a role.
 * <p>
 * Method workflows are intentionally deferred until the role membership
 * service and operator boundaries are implemented.
 * </p>
 */
@Path("/console/roles/{roleId}/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleMemberEndpoint {

    /**
     * Pages users assigned to a role.
     *
     * @param roleId  role identifier
     * @param request member page query
     * @return assigned user page
     */
    @GET
    public R<PageResult<RoleMemberVo>> pageRoleMembers(
            @PathParam("roleId") String roleId,
            @BeanParam RoleMemberPageRequest request
    ) {
        // TODO Implement role member paging through the role membership service.
        throw new UnsupportedOperationException("Role member paging is not implemented");
    }

    /**
     * Adds users to a role while retaining existing members.
     *
     * @param roleId  role identifier
     * @param request users to add
     * @return empty success response
     */
    @POST
    public R<Void> addRoleMembers(
            @PathParam("roleId") String roleId,
            RoleMemberAddRequest request
    ) {
        // TODO Implement role member assignment through the role membership service.
        throw new UnsupportedOperationException("Role member assignment is not implemented");
    }

    /**
     * Removes one user from a role.
     *
     * @param roleId role identifier
     * @param userId user identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{userId}")
    public R<Void> removeRoleMember(
            @PathParam("roleId") String roleId,
            @PathParam("userId") String userId
    ) {
        // TODO Implement role member removal through the role membership service.
        throw new UnsupportedOperationException("Role member removal is not implemented");
    }
}
