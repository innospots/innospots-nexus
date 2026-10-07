package com.innospots.nexus.spring.service.http.webflux;

import java.time.Duration;
import java.time.Instant;

import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.http.header.StandardHeaders;
import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycle;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;

import reactor.core.publisher.Mono;

/**
 * 建立 requestId、ServiceContext 与访问日志。
 */
public final class ServiceWebFilter implements WebFilter, Ordered {

    private final ServiceTransportSupport transportSupport;
    private final ThreadBoundServiceContext contexts;
    private final AccessLogWriter accessLogWriter;
    private final MdcContextBridge mdcContextBridge;

    /**
     * 创建 WebFlux 过滤器。
     */
    public ServiceWebFilter(
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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        CancellationSource cancellationSource = new CancellationSource();
        ServiceContext context = transportSupport.buildContext(exchange.getRequest(), cancellationSource);
        ServiceRequestLifecycle lifecycle = new ServiceRequestLifecycle(context.requestId(), cancellationSource);
        exchange.getAttributes().put(ServiceRequestLifecycle.REQUEST_ATTRIBUTE, lifecycle);
        exchange.getResponse().getHeaders().add(StandardHeaders.REQUEST_ID, context.requestId());
        ContextSnapshot snapshot = contexts.install(context);
        MdcContextBridge.MdcSnapshot mdcSnapshot = mdcContextBridge.install(context);
        Instant started = Instant.now();
        ServiceReactiveExchangeHolder.set(exchange);
        return chain.filter(exchange)
                .contextWrite(reactor.util.context.Context.of(ServiceReactiveKeys.SERVICE_CONTEXT, context))
                .doFinally(signal -> {
                    ServiceReactiveExchangeHolder.clear();
                    contexts.restore(snapshot);
                    mdcSnapshot.restore();
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    int statusCode = status != null ? status.value() : 200;
                    accessLogWriter.write(new AccessLogWriter.AccessLogEntry(
                            context.requestId(),
                            exchange.getRequest().getMethod().name(),
                            exchange.getRequest().getURI().getRawPath(),
                            statusCode,
                            resultLabel(statusCode),
                            Duration.between(started, Instant.now()),
                            context.security().id(),
                            context.trace().traceId(),
                            Instant.now()));
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    /**
     * 客户端断开时请求取消。
     *
     * @param exchange WebFlux exchange
     */
    public static void cancelIfClientDisconnected(ServerWebExchange exchange) {
        Object value = exchange.getAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE);
        if (value instanceof ServiceRequestLifecycle lifecycle) {
            lifecycle.cancellationSource().cancel(CancellationReason.CLIENT_DISCONNECTED);
        }
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
