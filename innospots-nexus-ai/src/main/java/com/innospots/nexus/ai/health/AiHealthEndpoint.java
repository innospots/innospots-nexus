package com.innospots.nexus.ai.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.ai.runtime.AiRuntime;
import com.innospots.nexus.ai.runtime.AiRuntimeHealth;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;

/**
 * Java 本地健康检查端点，不依赖 Node 存活。
 */
@Path("/ai/v1")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Nexus AI Health", description = "AI runtime health")
@EndpointModule(value = "ai")
public final class AiHealthEndpoint {

    private final AiRuntime runtime;
    private final String version;

    public AiHealthEndpoint(AiRuntime runtime, String version) {
        this.runtime = runtime;
        this.version = version;
    }

    @GET
    @Path("/health/live")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.health.live",
            ignoreAuth = true, ignorePermission = true, recordArgs = false)
    @Operation(operationId = "aiHealthLive", summary = "Liveness probe")
    public Response live() {
        return Response.ok(Map.of("status", "UP", "service", "innospots-nexus-ai"))
                .cacheControl(noStore())
                .build();
    }

    @GET
    @Path("/health/ready")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.health.ready",
            ignoreAuth = true, ignorePermission = true, recordArgs = false)
    @Operation(operationId = "aiHealthReady", summary = "Readiness probe")
    public Response ready() {
        AiRuntimeHealth health = runtime.health();
        Map<String, Object> body = new LinkedHashMap<>();
        if (health.ready()) {
            body.put("status", "UP");
            body.put("state", health.state().name());
            body.put("configVersion", health.configVersion());
            return Response.ok(body).cacheControl(noStore()).build();
        }
        body.put("status", "DOWN");
        body.put("reason", health.state() == AiRuntimeStatus.STARTING ? "runtime_starting" : "runtime_unavailable");
        body.put("state", health.state().name());
        body.put("retryAfterSeconds", 5);
        return Response.status(503).entity(body).cacheControl(noStore()).build();
    }

    @GET
    @Path("/health")
    @EndpointGrant(action = EndpointGrantAction.READ, name = "ai.health",
            ignoreAuth = true, ignorePermission = true, recordArgs = false)
    @Operation(operationId = "aiHealth", summary = "Aggregated health")
    public Response health() {
        AiRuntimeHealth health = runtime.health();
        Map<String, Object> runtimeView = new LinkedHashMap<>();
        runtimeView.put("state", health.state().name());
        runtimeView.put("version", health.runtimeVersion());
        runtimeView.put("nodeVersion", health.nodeVersion());
        runtimeView.put("pid", health.pid());
        runtimeView.put("lastCheckAt", health.lastCheckAt() == null ? null : health.lastCheckAt().toString());
        runtimeView.put("latencyMs", health.latencyMs());
        runtimeView.put("error", health.error());
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("process", health.state() == AiRuntimeStatus.FAILED ? "DOWN" : "UP");
        checks.put("http", health.state() == AiRuntimeStatus.RUNNING ? "UP" : "DOWN");
        checks.put("config", health.error() == null ? "UP" : "DEGRADED");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", health.state() == AiRuntimeStatus.RUNNING ? "UP"
                : health.state() == AiRuntimeStatus.DEGRADED ? "DEGRADED" : "DOWN");
        body.put("service", "innospots-nexus-ai");
        body.put("version", version);
        body.put("checkedAt", Instant.now().toString());
        body.put("runtime", runtimeView);
        body.put("checks", checks);
        int status = health.state() == AiRuntimeStatus.RUNNING ? 200 : 503;
        return Response.status(status).entity(body).cacheControl(noStore()).build();
    }

    private static CacheControl noStore() {
        CacheControl control = new CacheControl();
        control.setNoStore(true);
        return control;
    }
}
