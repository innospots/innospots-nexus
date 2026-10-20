package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.endpoint.OrganizationUnitGrantManagementEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Path(PlatformConstant.ORGANIZATION_UNIT_PERMISSIONS_PATH)
@Tag(name = "PlatformPermission", description = "运营平台授权")
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
