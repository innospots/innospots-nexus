package com.innospots.nexus.console.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 当前用户可见导航菜单 REST 契约；类级 {@code @Path} 由 platform 实现绑定。
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Navigation", description = "运行时导航")
@NexusAuthenticatedApi
public interface NavigationMenuEndpoint {

    @GET
    @Operation(operationId = "navigationMenuList", summary = "当前用户可见导航菜单")
    R<List<NavigationMenuVo>> listNavigationMenus();
}
