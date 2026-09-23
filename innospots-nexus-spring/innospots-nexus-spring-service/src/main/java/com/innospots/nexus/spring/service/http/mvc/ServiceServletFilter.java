package com.innospots.nexus.spring.service.http.mvc;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 建立 requestId、ServiceContext 与访问日志。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public final class ServiceServletFilter extends OncePerRequestFilter {

    private final ServiceTransportSupport transportSupport;
    private final ThreadBoundServiceContext contexts;
    private final AccessLogWriter accessLogWriter;
    private final MdcContextBridge mdcContextBridge;

    /**
     * 创建 Servlet 过滤器。
     */
    public ServiceServletFilter(
            ServiceTransportSupport transportSupport,
            ThreadBoundServiceContext contexts,
            AccessLogWriter accessLogWriter,
            MdcContextBridge mdcContextBridge) {
        this.transportSupport = Checks.notNull(transportSupport, "transportSupport");
        this.contexts = Checks.notNull(contexts, "contexts");
        this.accessLogWriter = Checks.notNull(accessLogWriter, "accessLogWriter");
        this.mdcContextBridge = Checks.notNull(mdcContextBridge, "mdcContextBridge");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        CancellationSource cancellationSource = new CancellationSource();
        ServiceContext context = transportSupport.buildContext(request, cancellationSource);
        ServiceRequestLifecycle lifecycle = new ServiceRequestLifecycle(context.requestId(), cancellationSource);
        request.setAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE, lifecycle);
        response.setHeader(StandardHeaders.REQUEST_ID, context.requestId());
        ContextSnapshot snapshot = contexts.install(context);
        MdcContextBridge.MdcSnapshot mdcSnapshot = mdcContextBridge.install(context);
        Instant started = Instant.now();
        try {
            filterChain.doFilter(request, response);
        } finally {
            contexts.restore(snapshot);
            mdcSnapshot.restore();
            accessLogWriter.write(new AccessLogWriter.AccessLogEntry(
                    context.requestId(),
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    resultLabel(response.getStatus()),
                    Duration.between(started, Instant.now()),
                    context.security().id(),
                    context.trace().traceId(),
                    Instant.now()));
        }
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        doFilterInternal(request, response, filterChain);
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

    /**
     * 客户端断开时请求取消。
     */
    public static void cancelIfClientDisconnected(HttpServletRequest request) {
        Object value = request.getAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE);
        if (value instanceof ServiceRequestLifecycle lifecycle) {
            lifecycle.cancellationSource().cancel(CancellationReason.CLIENT_DISCONNECTED);
        }
    }
}
