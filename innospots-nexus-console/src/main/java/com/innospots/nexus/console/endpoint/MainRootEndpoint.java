package com.innospots.nexus.console.endpoint;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

/**
 * 站点根路径 {@code GET /}：重定向至配置的文档入口（见 {@code nexus.console.web.jersey.root-path}）。
 */
@Path("/")
public final class MainRootEndpoint {

    private final String rootRedirectPath;

    public MainRootEndpoint(String rootRedirectPath) {
        this.rootRedirectPath = rootRedirectPath;
    }

    @GET
    public Response redirectRoot() {
        String location = rootRedirectPath;
        if (location == null || location.isBlank()) {
            location = OpenApiCatalogPaths.UI_DEFAULT;
        }
        return Response.temporaryRedirect(URI.create(location)).build();
    }
}
