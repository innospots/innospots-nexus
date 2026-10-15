package com.innospots.nexus.platform.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.console.permission.endpoint.CurrentAuthorizationEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Path(PlatformConstant.ME_PERMISSIONS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformCurrentAuthorizationEndpoint implements CurrentAuthorizationEndpoint {

    private final PlatformPermissionService platformPermissionService;

    @Override
    @GET
    @Operation(operationId = "platformPermissionVisibleList", summary = "当前平台用户可见权限资源")
    public R<List<PermissionResourceVo>> listResources() {
        return R.ok(platformPermissionService.listCurrentUserResources());
    }
}
