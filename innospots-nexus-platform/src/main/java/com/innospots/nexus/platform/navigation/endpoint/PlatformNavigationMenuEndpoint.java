package com.innospots.nexus.platform.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.navigation.service.PlatformNavigationService;

@Path(PlatformConstant.NAVIGATION_MENUS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformNavigation", description = "运营平台运行时导航")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformNavigationMenuEndpoint implements NavigationMenuEndpoint {

    private final PlatformNavigationService platformNavigationService;

    @Override
    @GET
    @Operation(operationId = "platformNavigationMenuList", summary = "当前平台用户可见导航菜单")
    public R<List<NavigationMenuVo>> listNavigationMenus() {
        return R.ok(platformNavigationService.listNavigationMenus());
    }
}
