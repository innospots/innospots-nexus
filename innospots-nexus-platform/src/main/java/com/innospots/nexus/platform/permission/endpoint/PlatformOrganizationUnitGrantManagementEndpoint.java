package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.endpoint.OrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@EndpointModule(value = "permission", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
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
    public R<PermissionGrantReplaceRequest> getOrganizationUnitPermissions(String unitId) {
        return R.ok(platformPermissionService.listOrganizationUnitPermissions(unitId));
    }

    @Override
    public R<Void> replaceOrganizationUnitPermissions(String unitId, PermissionGrantReplaceRequest request) {
        platformPermissionService.replaceOrganizationUnitPermissions(unitId, request);
        return R.ok();
    }
}
