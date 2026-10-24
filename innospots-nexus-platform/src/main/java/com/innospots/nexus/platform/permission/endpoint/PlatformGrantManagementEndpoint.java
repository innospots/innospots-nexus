package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@EndpointModule(value = "permission", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
@Path(PlatformConstant.ROLE_PERMISSIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@NexusAuthenticatedApi
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
