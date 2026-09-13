package com.innospots.nexus.console.scope.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.SelectProjectRequest;
import com.innospots.nexus.console.auth.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * Tenant-realm workspace and project scope selection.
 */
@Path("/tenant/scope")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TenantScopeEndpoint {

    /**
     * Activates a workspace within the current tenant business session.
     *
     * @param request tenant and workspace to activate
     * @return workspace-scoped business token pair
     */
    @POST
    @Path("/select-workspace")
    R<AuthTokenVo> selectWorkspace(SelectWorkspaceRequest request);

    /**
     * Activates a project within the current workspace session.
     *
     * @param request tenant, workspace, and project to activate
     * @return project-scoped business token pair
     */
    @POST
    @Path("/select-project")
    R<AuthTokenVo> selectProject(SelectProjectRequest request);
}
