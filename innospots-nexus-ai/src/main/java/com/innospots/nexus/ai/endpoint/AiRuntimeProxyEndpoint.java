package com.innospots.nexus.ai.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.ai.proxy.AiProxyService;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;

/**
 * 只读/OpenAPI 代理端点。
 */
@Path("/ai/v1")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Nexus AI", description = "AI runtime proxy")
@EndpointModule(value = "ai")
public final class AiRuntimeProxyEndpoint {

    private final AiProxyService proxy;

    public AiRuntimeProxyEndpoint(AiProxyService proxy) {
        this.proxy = proxy;
    }

    @GET
    @Path("/openapi.yaml")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.openapi",
            ignoreAuth = true, ignorePermission = true, recordArgs = false)
    @Operation(operationId = "aiOpenapi", summary = "AI OpenAPI")
    public void openapi(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/openapi.yaml", uriInfo, headers, null, response);
    }

    @GET
    @Path("/agents")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.read", recordArgs = false)
    @Operation(operationId = "aiAgents", summary = "List agent profiles")
    public void agents(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/agents", uriInfo, headers, null, response);
    }
}
