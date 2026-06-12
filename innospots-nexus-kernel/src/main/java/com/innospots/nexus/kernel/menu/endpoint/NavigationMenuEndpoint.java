package com.innospots.nexus.kernel.menu.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.menu.domain.vo.NavigationMenuVo;

/**
 * Read-only endpoint for the current management user's visible navigation.
 * <p>
 * Permission filtering is intentionally deferred to the permission domain.
 * </p>
 */
@Path("/console/navigation/menus")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NavigationMenuEndpoint {

    /**
     * Lists enabled and visible navigation nodes for the current request.
     *
     * @return available navigation tree
     */
    @GET
    public R<List<NavigationMenuVo>> listNavigationMenus() {
        // TODO Implement navigation assembly and permission filtering.
        throw new UnsupportedOperationException("Navigation menu query is not implemented");
    }
}
