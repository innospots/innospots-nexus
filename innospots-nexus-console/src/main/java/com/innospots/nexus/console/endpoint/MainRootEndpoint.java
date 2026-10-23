package com.innospots.nexus.console.endpoint;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

/**
 * 站点根路径 {@code GET /}：重定向至配置的文档入口（见 {@code nexus.console.web.jersey.root-path}）。
 */
@EndpointModule(value = "console", ignoreAuth = true, ignorePermission = true)
@Path("/")
public final class MainRootEndpoint {

    private final String rootRedirectPath;

    public MainRootEndpoint(String rootRedirectPath) {
        this.rootRedirectPath = rootRedirectPath;
    }

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "console.root.redirect",
            label = @I18nLiteral(pairs = {"zh-CN", "站点根路径重定向", "en", "Root path redirect"}))
    public Response redirectRoot() {
        String location = rootRedirectPath;
        if (location == null || location.isBlank()) {
            location = OpenApiCatalogPaths.UI_DEFAULT;
        }
        return Response.temporaryRedirect(URI.create(location)).build();
    }
}
