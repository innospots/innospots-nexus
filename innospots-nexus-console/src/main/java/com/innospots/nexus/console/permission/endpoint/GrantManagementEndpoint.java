package com.innospots.nexus.console.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 角色与组织单元权限全量替换的管理 REST 资源。
 */
@Path("/console")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class GrantManagementEndpoint {

    private final PermissionGrantService grantService;

    @GET
    @Path("/roles/{roleId}/permissions")
    @Operation(operationId = "permissionGrantListByRole", summary = "查询角色授权")
    public R<PermissionGrantReplaceRequest> getRolePermissions(@PathParam("roleId") String roleId) {
        return R.ok(grantService.list(PermissionSubjectType.ROLE, roleId));
    }

    @PUT
    @Path("/roles/{roleId}/permissions")
    @Operation(operationId = "permissionGrantReplaceByRole", summary = "全量替换角色授权")
    public R<Void> replaceRolePermissions(
            @PathParam("roleId") String roleId,
            PermissionGrantReplaceRequest request
    ) {
        grantService.replace(PermissionSubjectType.ROLE, roleId, request);
        return R.ok();
    }

    @GET
    @Path("/organization-units/{unitId}/permissions")
    @Operation(operationId = "permissionGrantListByOrgUnit", summary = "查询组织单元授权")
    public R<PermissionGrantReplaceRequest> getOrganizationUnitPermissions(@PathParam("unitId") String unitId) {
        return R.ok(grantService.list(PermissionSubjectType.ORG_UNIT, unitId));
    }

    @PUT
    @Path("/organization-units/{unitId}/permissions")
    @Operation(operationId = "permissionGrantReplaceByOrgUnit", summary = "全量替换组织单元授权")
    public R<Void> replaceOrganizationUnitPermissions(
            @PathParam("unitId") String unitId,
            PermissionGrantReplaceRequest request
    ) {
        grantService.replace(PermissionSubjectType.ORG_UNIT, unitId, request);
        return R.ok();
    }
}
