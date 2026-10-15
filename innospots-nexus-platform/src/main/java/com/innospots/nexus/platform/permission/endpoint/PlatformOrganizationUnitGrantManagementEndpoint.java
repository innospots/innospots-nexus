package com.innospots.nexus.platform.permission.endpoint;

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
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.endpoint.OrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Path(PlatformConstant.ORGANIZATION_UNIT_PERMISSIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformOrganizationUnitGrantManagementEndpoint
        implements OrganizationUnitGrantManagementEndpoint {

    private final PlatformPermissionService platformPermissionService;

    @Override
    @GET
    @Operation(operationId = "platformPermissionGrantListByOrgUnit", summary = "查询平台组织单元授权")
    public R<PermissionGrantReplaceRequest> getOrganizationUnitPermissions(@PathParam("unitId") String unitId) {
        return R.ok(platformPermissionService.listOrganizationUnitPermissions(unitId));
    }

    @Override
    @PUT
    @Operation(operationId = "platformPermissionGrantReplaceByOrgUnit", summary = "全量替换平台组织单元授权")
    public R<Void> replaceOrganizationUnitPermissions(
            @PathParam("unitId") String unitId,
            PermissionGrantReplaceRequest request) {
        platformPermissionService.replaceOrganizationUnitPermissions(unitId, request);
        return R.ok();
    }
}
