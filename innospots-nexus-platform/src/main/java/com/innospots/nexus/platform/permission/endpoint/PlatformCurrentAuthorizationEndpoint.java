package com.innospots.nexus.platform.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.console.permission.endpoint.CurrentAuthorizationEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPermissionService;

@Path(PlatformConstant.ME_PERMISSIONS_PATH)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@RequiredArgsConstructor
public class PlatformCurrentAuthorizationEndpoint implements CurrentAuthorizationEndpoint {

    private final PlatformPermissionService platformPermissionService;

    @Override
    public R<List<PermissionResourceVo>> listResources() {
        return R.ok(platformPermissionService.listCurrentUserResources());
    }
}
