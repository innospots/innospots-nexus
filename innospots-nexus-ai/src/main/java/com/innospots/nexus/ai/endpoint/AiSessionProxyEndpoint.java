package com.innospots.nexus.ai.endpoint;

import java.io.InputStream;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
 * 会话与 run 代理端点。
 */
@Path("/ai/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Nexus AI Sessions", description = "AI sessions and runs")
@EndpointModule(value = "ai")
public final class AiSessionProxyEndpoint {

    private final AiProxyService proxy;

    public AiSessionProxyEndpoint(AiProxyService proxy) {
        this.proxy = proxy;
    }

    @GET
    @Path("/sessions")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.read", recordArgs = false)
    @Operation(operationId = "aiSessions", summary = "List sessions")
    public void listSessions(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/sessions", uriInfo, headers, null, response);
    }

    @POST
    @Path("/sessions")
    @EndpointGrant(action = EndpointGrantAction.CREATE, name = "ai.write", recordArgs = false, auditLog = true)
    @Operation(operationId = "aiSessionCreate", summary = "Create session")
    public void createSession(@Context UriInfo uriInfo, @Context HttpHeaders headers, InputStream body,
                              @Suspended AsyncResponse response) {
        proxy.forward("POST", "/v1/sessions", uriInfo, headers, body, response);
    }

    @GET
    @Path("/sessions/{id}")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.read", recordArgs = false)
    @Operation(operationId = "aiSessionGet", summary = "Get session")
    public void getSession(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                           @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/sessions/" + id, uriInfo, headers, null, response);
    }

    @DELETE
    @Path("/sessions/{id}")
    @EndpointGrant(action = EndpointGrantAction.DELETE, name = "ai.write", recordArgs = false, auditLog = true)
    @Operation(operationId = "aiSessionDelete", summary = "Delete session")
    public void deleteSession(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                              @Suspended AsyncResponse response) {
        proxy.forward("DELETE", "/v1/sessions/" + id, uriInfo, headers, null, response);
    }

    @GET
    @Path("/sessions/{id}/messages")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.read", recordArgs = false)
    @Operation(operationId = "aiMessages", summary = "List messages")
    public void listMessages(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                             @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/sessions/" + id + "/messages", uriInfo, headers, null, response);
    }

    @POST
    @Path("/sessions/{id}/messages")
    @EndpointGrant(action = EndpointGrantAction.CREATE, name = "ai.write", recordArgs = false, auditLog = true)
    @Operation(operationId = "aiMessageCreate", summary = "Send message")
    public void sendMessage(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                            InputStream body, @Suspended AsyncResponse response) {
        proxy.forward("POST", "/v1/sessions/" + id + "/messages", uriInfo, headers, body, response);
    }

    @GET
    @Path("/runs/{id}")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.read", recordArgs = false)
    @Operation(operationId = "aiRunGet", summary = "Get run")
    public void getRun(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                       @Suspended AsyncResponse response) {
        proxy.forward("GET", "/v1/runs/" + id, uriInfo, headers, null, response);
    }

    @POST
    @Path("/runs/{id}/cancel")
    @EndpointGrant(action = EndpointGrantAction.UPDATE, name = "ai.write", recordArgs = false, auditLog = true)
    @Operation(operationId = "aiRunCancel", summary = "Cancel run")
    public void cancelRun(@PathParam("id") String id, @Context UriInfo uriInfo, @Context HttpHeaders headers,
                          @Suspended AsyncResponse response) {
        proxy.forward("POST", "/v1/runs/" + id + "/cancel", uriInfo, headers, null, response);
    }
}
