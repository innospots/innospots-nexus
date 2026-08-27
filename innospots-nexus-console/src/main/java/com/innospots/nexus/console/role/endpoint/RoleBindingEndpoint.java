package com.innospots.nexus.console.role.endpoint;

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
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;

/**
 * Management-console endpoint for USER and ORG_UNIT role bindings.
 * <p>
 * Method workflows are deferred until the role-binding service and operator
 * boundaries are implemented.
 * </p>
 */
@Path("/console/roles/{roleId}/bindings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleBindingEndpoint {

    /**
     * Pages subjects bound to a role.
     *
     * @param roleId  role identifier
     * @param request binding page query
     * @return bound subject page
     */
    @GET
    public R<PageResult<RoleBindingVo>> pageRoleBindings(
            @PathParam("roleId") String roleId,
            @BeanParam RoleBindingPageRequest request
    ) {
        throw new UnsupportedOperationException("Role binding paging is not implemented");
    }

    /**
     * Adds subjects to a role while retaining existing bindings.
     *
     * @param roleId  role identifier
     * @param request subjects to bind
     * @return empty success response
     */
    @POST
    public R<Void> addRoleBindings(
            @PathParam("roleId") String roleId,
            RoleBindingAddRequest request
    ) {
        throw new UnsupportedOperationException("Role binding assignment is not implemented");
    }

    /**
     * Removes one binding from a role.
     *
     * @param roleId    role identifier
     * @param bindingId binding identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{bindingId}")
    public R<Void> removeRoleBinding(
            @PathParam("roleId") String roleId,
            @PathParam("bindingId") String bindingId
    ) {
        throw new UnsupportedOperationException("Role binding removal is not implemented");
    }
}
