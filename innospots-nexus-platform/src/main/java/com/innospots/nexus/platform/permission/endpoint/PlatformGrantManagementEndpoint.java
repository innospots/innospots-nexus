package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Path(PlatformConstant.ROLE_PERMISSIONS_PATH)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@RequiredArgsConstructor
public class PlatformGrantManagementEndpoint implements GrantManagementEndpoint {

    private final PlatformPermissionService platformPermissionService;

    @Override
    public R<PermissionGrantReplaceRequest> getRolePermissions(String roleId) {
        return R.ok(platformPermissionService.listRolePermissions(roleId));
    }

    @Override
    public R<Void> replaceRolePermissions(String roleId, PermissionGrantReplaceRequest request) {
        platformPermissionService.replaceRolePermissions(roleId, request);
        return R.ok();
    }
}
