package com.innospots.nexus.kernel.role.endpoint;

import java.util.List;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.kernel.role.domain.request.RolePageRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.kernel.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.kernel.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.kernel.role.domain.vo.RoleVo;

/**
 * Management-console contract for role lifecycle and role lookup operations.
 */
@Path("/console/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RoleEndpoint {

    /**
     * Pages project roles using management filters.
     *
     * @param request role page query
     * @return matching role page
     */
    @GET
    R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request);

    /**
     * Returns one role.
     *
     * @param roleId role identifier
     * @return role details
     */
    @GET
    @Path("/{roleId}")
    R<RoleVo> getRole(@PathParam("roleId") String roleId);

    /**
     * Creates a project role.
     *
     * @param request role creation data
     * @return created role
     */
    @POST
    R<RoleVo> createRole(RoleCreateRequest request);

    /**
     * Updates mutable role profile fields.
     *
     * @param roleId  role identifier
     * @param request role update data
     * @return updated role
     */
    @PUT
    @Path("/{roleId}")
    R<RoleVo> updateRole(
            @PathParam("roleId") String roleId,
            RoleUpdateRequest request
    );

    /**
     * Enables or disables a role.
     *
     * @param roleId  role identifier
     * @param request target status
     * @return empty success response
     */
    @PUT
    @Path("/{roleId}/status")
    R<Void> updateRoleStatus(
            @PathParam("roleId") String roleId,
            RoleStatusUpdateRequest request
    );

    /**
     * Deletes a non-protected role.
     *
     * @param roleId role identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{roleId}")
    R<Void> deleteRole(@PathParam("roleId") String roleId);

    /**
     * Lists compact role options for assignment controls.
     *
     * @param status optional status filter
     * @return role options
     */
    @GET
    @Path("/options")
    R<List<RoleOptionVo>> listRoleOptions(@QueryParam("status") BasicStatus status);
}
