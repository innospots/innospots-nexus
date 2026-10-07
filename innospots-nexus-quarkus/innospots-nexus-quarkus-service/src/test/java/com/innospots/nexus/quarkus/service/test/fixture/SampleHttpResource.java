package com.innospots.nexus.quarkus.service.test.fixture;

import java.lang.reflect.Method;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.quarkus.service.config.ServiceRuntimeHolder;
import com.innospots.nexus.quarkus.service.invocation.ServiceInvocationBridge;
import com.innospots.nexus.quarkus.service.rest.ServiceRequestLifecycle;
import com.innospots.nexus.service.adapter.test.fixture.AdapterDownloadFixtures;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.transfer.download.DownloadResource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * adapter-test HTTP 夹具端点。
 */
@ApplicationScoped
@Path("")
public class SampleHttpResource {

    private final ServiceInvocationBridge invocationBridge;
    private final ServiceRuntimeHolder serviceRuntimeHolder;

    public SampleHttpResource(ServiceInvocationBridge invocationBridge, ServiceRuntimeHolder serviceRuntimeHolder) {
        this.invocationBridge = invocationBridge;
        this.serviceRuntimeHolder = serviceRuntimeHolder;
    }

    @GET
    @Path(AdapterScenarioPaths.UNAUGMENTED)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> unaugmented() {
        return Map.of("status", "ok");
    }

    @GET
    @Path(AdapterScenarioPaths.CONTEXT)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> context() {
        return Map.of("requestId", serviceRuntimeHolder.contexts().requireCurrent().requestId());
    }

    @GET
    @Path(AdapterScenarioPaths.ERROR_UNAUTHORIZED)
    public void unauthorized() {
        throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
    }

    @GET
    @Path(AdapterScenarioPaths.ERROR_FORBIDDEN)
    public void forbidden() {
        throw NexusException.build(NexusStatusCode.NO_PERMISSION);
    }

    @GET
    @Path(AdapterScenarioPaths.ERROR_NOT_FOUND)
    public void notFound() {
        throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
    }

    @GET
    @Path(AdapterScenarioPaths.ERROR_RATE_LIMIT)
    public void rateLimitError() {
        throw NexusException.build(NexusStatusCode.LIMIT_EXCEEDED);
    }

    @GET
    @Path(AdapterScenarioPaths.ERROR_INTERNAL)
    public void internalError() {
        throw NexusException.build(NexusStatusCode.SYSTEM_ERROR);
    }

    @GET
    @Path(AdapterScenarioPaths.SECURE)
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermission("adapter.secure")
    public Map<String, String> secure() throws NoSuchMethodException {
        Method method = SampleHttpResource.class.getMethod("secure");
        return invocationBridge.invoke(this, method, "adapter.secure", () -> Map.of("status", "ok"));
    }

    @GET
    @Path(AdapterScenarioPaths.SLOW)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> slow(
            @QueryParam("delayMs") @DefaultValue("5000") long delayMs,
            @Context ContainerRequestContext requestContext) throws NoSuchMethodException {
        Method method = SampleHttpResource.class.getMethod("slow", long.class, ContainerRequestContext.class);
        return invocationBridge.invoke(this, method, "adapter.slow", () -> {
            long remaining = delayMs;
            while (remaining > 0) {
                if (serviceRuntimeHolder.contexts().requireCurrent().cancellation().isCancelled()) {
                    cancelIfClientDisconnected(requestContext);
                    break;
                }
                long step = Math.min(remaining, 50L);
                sleep(step);
                remaining -= step;
            }
            return Map.of("status", "done");
        });
    }

    @GET
    @Path(AdapterScenarioPaths.GOVERNANCE_RATE_LIMIT)
    @Produces(MediaType.APPLICATION_JSON)
    @RateLimited("adapter-rate-limit")
    public Map<String, String> governanceRateLimit() throws NoSuchMethodException {
        Method method = SampleHttpResource.class.getMethod("governanceRateLimit");
        return invocationBridge.invoke(this, method, "adapter.governance.rate-limit", () -> Map.of("status", "ok"));
    }

    @GET
    @Path(AdapterScenarioPaths.GOVERNANCE_TIMEOUT)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> governanceTimeout() throws NoSuchMethodException {
        Method method = SampleHttpResource.class.getMethod("governanceTimeout");
        return invocationBridge.invoke(this, method, "adapter.governance.timeout", () -> {
            long remaining = 2_000L;
            while (remaining > 0) {
                if (serviceRuntimeHolder.contexts().requireCurrent().cancellation().isCancelled()) {
                    throw NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED);
                }
                long step = Math.min(remaining, 50L);
                sleep(step);
                remaining -= step;
            }
            return Map.of("status", "ok");
        });
    }

    @GET
    @Path(AdapterScenarioPaths.OBSERVABILITY_TRACE)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> observabilityTrace() {
        return Map.of("status", "ok");
    }

    @GET
    @Path(AdapterScenarioPaths.DOWNLOAD)
    public DownloadResource download() {
        return AdapterDownloadFixtures.memoryDownload();
    }

    private static void cancelIfClientDisconnected(ContainerRequestContext requestContext) {
        Object value = requestContext.getProperty(ServiceRequestLifecycle.REQUEST_PROPERTY);
        if (value instanceof ServiceRequestLifecycle lifecycle) {
            lifecycle.cancellationSource().cancel(CancellationReason.CLIENT_DISCONNECTED);
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
