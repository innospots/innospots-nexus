package com.innospots.nexus.portal.scope.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.portal.scope.domain.request.SelectProjectRequest;
import com.innospots.nexus.portal.scope.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

/**
 * 租户域工作区与项目作用域选择。
 */
@Path("/tenant/scope")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "TenantScope", description = "工作区与项目作用域选择")
@NexusAuthenticatedApi
public interface TenantScopeEndpoint {

    /**
     * 在当前租户业务会话内激活工作区。
     *
     * @param request 待激活的租户与工作区
     * @return workspace-scoped 业务令牌对
     */
    @POST
    @Path("/select-workspace")
    @Operation(operationId = "tenantScopeSelectWorkspace", summary = "选择工作区")
    R<AuthTokenVo> selectWorkspace(SelectWorkspaceRequest request);

    /**
     * 在当前工作区会话内激活项目。
     *
     * @param request 待激活的租户、工作区与项目
     * @return project-scoped 业务令牌对
     */
    @POST
    @Path("/select-project")
    R<AuthTokenVo> selectProject(SelectProjectRequest request);
}
