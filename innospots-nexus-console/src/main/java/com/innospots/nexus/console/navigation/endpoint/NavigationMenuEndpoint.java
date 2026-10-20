package com.innospots.nexus.console.navigation.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 当前用户可见导航菜单 REST 契约；类级 {@code @Path} 由 platform 实现绑定。
 */
@EndpointModule("menu")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Navigation", description = "运行时导航")
@NexusAuthenticatedApi
public interface NavigationMenuEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "navigation.menu.list",
            label = @I18nLiteral(pairs = {"zh-CN", "当前用户可见导航菜单", "en", "List navigation menus"}),
            ignorePermission = true)
    @Operation(operationId = "navigationMenuList", summary = "当前用户可见导航菜单")
    R<List<NavigationMenuVo>> listNavigationMenus();
}
