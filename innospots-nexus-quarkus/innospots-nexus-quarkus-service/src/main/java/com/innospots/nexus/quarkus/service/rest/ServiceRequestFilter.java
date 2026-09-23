package com.innospots.nexus.quarkus.service.rest;

import java.time.Duration;
import java.time.Instant;

import com.innospots.nexus.quarkus.service.config.ServiceRuntimeHolder;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * 建立 requestId、ServiceContext 与访问日志。
 */
@Provider
@Priority(Priorities.USER)
@ApplicationScoped
public final class ServiceRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    static final String CONTEXT_SNAPSHOT_PROPERTY = ServiceRequestFilter.class.getName() + ".contextSnapshot";
    static final String MDC_SNAPSHOT_PROPERTY = ServiceRequestFilter.class.getName() + ".mdcSnapshot";

    private final ServiceTransportSupport transportSupport;
    private final ServiceRuntimeHolder serviceRuntimeHolder;
    private final AccessLogWriter accessLogWriter;
    private final MdcContextBridge mdcContextBridge;

    public ServiceRequestFilter(
            ServiceTransportSupport transportSupport,
            ServiceRuntimeHolder serviceRuntimeHolder,
            AccessLogWriter accessLogWriter,
            MdcContextBridge mdcContextBridge) {
        this.transportSupport = Checks.notNull(transportSupport, "transportSupport");
        this.serviceRuntimeHolder = Checks.notNull(serviceRuntimeHolder, "serviceRuntimeHolder");
        this.accessLogWriter = Checks.notNull(accessLogWriter, "accessLogWriter");
        this.mdcContextBridge = Checks.notNull(mdcContextBridge, "mdcContextBridge");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        CancellationSource cancellationSource = new CancellationSource();
        ServiceContext context = transportSupport.buildContext(requestContext, cancellationSource);
        ServiceRequestLifecycle lifecycle = new ServiceRequestLifecycle(context.requestId(), cancellationSource);
        requestContext.setProperty(ServiceRequestLifecycle.REQUEST_PROPERTY, lifecycle);
        requestContext.setProperty(ServiceRequestLifecycle.STARTED_AT_PROPERTY, Instant.now());
        requestContext.setProperty(CONTEXT_SNAPSHOT_PROPERTY, serviceRuntimeHolder.contexts().install(context));
        requestContext.setProperty(MDC_SNAPSHOT_PROPERTY, mdcContextBridge.install(context));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object lifecycleValue = requestContext.getProperty(ServiceRequestLifecycle.REQUEST_PROPERTY);
        if (!(lifecycleValue instanceof ServiceRequestLifecycle lifecycle)) {
            return;
        }
        responseContext.getHeaders().putSingle(StandardHeaders.REQUEST_ID, lifecycle.requestId());
        Instant started = startedAt(requestContext);
        ServiceContext context = serviceRuntimeHolder.contexts().current().orElse(null);
        accessLogWriter.write(new AccessLogWriter.AccessLogEntry(
                lifecycle.requestId(),
                requestContext.getMethod(),
                requestContext.getUriInfo().getRequestUri().getPath(),
                responseContext.getStatus(),
                resultLabel(responseContext.getStatus()),
                Duration.between(started, Instant.now()),
                context == null ? "anonymous" : context.security().id(),
                context == null ? "" : context.trace().traceId(),
                Instant.now()));
        restoreContext(requestContext);
    }

    private void restoreContext(ContainerRequestContext requestContext) {
        Object contextSnapshot = requestContext.getProperty(CONTEXT_SNAPSHOT_PROPERTY);
        if (contextSnapshot instanceof com.innospots.nexus.service.runtime.context.ContextSnapshot snapshot) {
            serviceRuntimeHolder.contexts().restore(snapshot);
        }
        Object mdcSnapshot = requestContext.getProperty(MDC_SNAPSHOT_PROPERTY);
        if (mdcSnapshot instanceof MdcContextBridge.MdcSnapshot snapshot) {
            snapshot.restore();
        }
    }

    private static Instant startedAt(ContainerRequestContext requestContext) {
        Object value = requestContext.getProperty(ServiceRequestLifecycle.STARTED_AT_PROPERTY);
        if (value instanceof Instant instant) {
            return instant;
        }
        return Instant.now();
    }

    private static String resultLabel(int status) {
        if (status >= 500) {
            return "error";
        }
        if (status >= 400) {
            return "client-error";
        }
        return "success";
    }
}
