package com.innospots.nexus.platform.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.navigation.endpoint.NavigationMenuEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.navigation.service.PlatformNavigationService;

@Path(PlatformConstant.NAVIGATION_MENUS_PATH)
@Tag(name = "PlatformNavigation", description = "运营平台运行时导航")
@RequiredArgsConstructor
public class PlatformNavigationMenuEndpoint implements NavigationMenuEndpoint {

    private final PlatformNavigationService platformNavigationService;

    @Override
    public R<List<NavigationMenuVo>> listNavigationMenus() {
        return R.ok(platformNavigationService.listNavigationMenus());
    }
}
