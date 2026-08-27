package com.innospots.nexus.console.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;

/** 角色和组织单元权限全量替换的管理接口契约。 */
@Path("/console")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GrantManagementEndpoint {

    /**
     * 查询角色当前的全部资源授权及 datasource 附加查询条件。
     *
     * @param roleId 角色 ID
     * @return 角色的完整授权集合
     */
    @GET
    @Path("/roles/{roleId}/permissions")
    R<PermissionGrantReplaceRequest> getRolePermissions(@PathParam("roleId") String roleId);

    /**
     * 全量替换角色授权；请求中的 datasource 条件与资源授权一并保存。
     *
     * @param roleId 角色 ID
     * @param request 角色最终应拥有的完整授权集合
     * @return 空响应
     */
    @PUT
    @Path("/roles/{roleId}/permissions")
    R<Void> replaceRolePermissions(
            @PathParam("roleId") String roleId,
            PermissionGrantReplaceRequest request);

    /**
     * 查询组织单元当前的全部资源授权及 datasource 附加查询条件。
     *
     * @param unitId 组织单元 ID
     * @return 组织单元的完整授权集合
     */
    @GET
    @Path("/organization-units/{unitId}/permissions")
    R<PermissionGrantReplaceRequest> getOrganizationUnitPermissions(@PathParam("unitId") String unitId);

    /**
     * 全量替换组织单元授权；请求中的 datasource 条件与资源授权一并保存。
     *
     * @param unitId 组织单元 ID
     * @param request 组织单元最终应拥有的完整授权集合
     * @return 空响应
     */
    @PUT
    @Path("/organization-units/{unitId}/permissions")
    R<Void> replaceOrganizationUnitPermissions(
            @PathParam("unitId") String unitId,
            PermissionGrantReplaceRequest request);
}
