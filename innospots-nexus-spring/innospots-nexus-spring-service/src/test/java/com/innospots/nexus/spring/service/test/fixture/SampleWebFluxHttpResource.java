package com.innospots.nexus.spring.service.test.fixture;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.adapter.test.fixture.AdapterDownloadFixtures;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.contract.policy.annotation.RateLimited;
import com.innospots.nexus.service.contract.policy.annotation.ServiceOperation;
import com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected;
import com.innospots.nexus.service.contract.security.annotation.RequiresPermission;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.transfer.download.DownloadResource;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.spring.service.http.invocation.ServiceInvocationBridge;
import com.innospots.nexus.spring.service.http.webflux.ServiceReactiveExchangeHolder;
import com.innospots.nexus.spring.service.http.webflux.ServiceWebFilter;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * adapter-test WebFlux HTTP 夹具端点。
 */
@RestController
@Profile("webflux")
public class SampleWebFluxHttpResource {

    private final ServiceInvocationBridge invocationBridge;
    private final ServiceContextAccessor contextAccessor;
    private final ThreadBoundServiceContext contexts;

    public SampleWebFluxHttpResource(
            ServiceInvocationBridge invocationBridge,
            ServiceContextAccessor contextAccessor,
            ThreadBoundServiceContext contexts) {
        this.invocationBridge = invocationBridge;
        this.contextAccessor = contextAccessor;
        this.contexts = contexts;
    }

    @GetMapping(AdapterScenarioPaths.UNAUGMENTED)
    public Map<String, String> unaugmented() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.CONTEXT)
    public Map<String, String> context() {
        return Map.of("requestId", contextAccessor.requireCurrent().requestId());
    }

    @GetMapping(AdapterScenarioPaths.ERROR_UNAUTHORIZED)
    public Mono<Void> unauthorized() {
        return Mono.error(NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED));
    }

    @GetMapping(AdapterScenarioPaths.ERROR_FORBIDDEN)
    public Mono<Void> forbidden() {
        return Mono.error(NexusException.build(NexusStatusCode.NO_PERMISSION));
    }

    @GetMapping(AdapterScenarioPaths.ERROR_NOT_FOUND)
    public Mono<Void> notFound() {
        return Mono.error(NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND));
    }

    @GetMapping(AdapterScenarioPaths.ERROR_RATE_LIMIT)
    public Mono<Void> rateLimitError() {
        return Mono.error(NexusException.build(NexusStatusCode.LIMIT_EXCEEDED));
    }

    @GetMapping(AdapterScenarioPaths.ERROR_INTERNAL)
    public Mono<Void> internalError() {
        return Mono.error(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
    }

    @GetMapping(AdapterScenarioPaths.SECURE)
    @ServiceOperation("adapter.secure")
    @RequiresPermission("adapter.secure")
    public Map<String, String> secure() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.SLOW)
    public Mono<Map<String, String>> slow(@RequestParam(name = "delayMs", defaultValue = "5000") long delayMs)
            throws NoSuchMethodException {
        Method method = SampleWebFluxHttpResource.class.getMethod("slow", long.class);
        ServiceContext serviceContext = contextAccessor.requireCurrent();
        ServerWebExchange exchange = ServiceReactiveExchangeHolder.get();
        return onBoundedElastic(serviceContext, () -> invocationBridge.invoke(this, method, "adapter.slow", () -> {
            long remaining = delayMs;
            while (remaining > 0) {
                if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
                    if (exchange != null) {
                        ServiceWebFilter.cancelIfClientDisconnected(exchange);
                    }
                    break;
                }
                long step = Math.min(remaining, 50L);
                sleep(step);
                remaining -= step;
            }
            return Map.of("status", "done");
        }));
    }

    @GetMapping(AdapterScenarioPaths.GOVERNANCE_RATE_LIMIT)
    @ServiceOperation("adapter.governance.rate-limit")
    @RateLimited("adapter-rate-limit")
    public Map<String, String> governanceRateLimit() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.GOVERNANCE_TIMEOUT)
    @ServiceOperation("adapter.governance.timeout")
    @TimeoutProtected("adapter.governance.timeout")
    public Map<String, String> governanceTimeout() {
        ServiceContext serviceContext = contextAccessor.requireCurrent();
        return onBoundedElasticBlocking(serviceContext, () -> {
            long remaining = 2_000L;
            while (remaining > 0) {
                if (contextAccessor.requireCurrent().cancellation().isCancelled()) {
                    throw NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED);
                }
                long step = Math.min(remaining, 50L);
                sleep(step);
                remaining -= step;
            }
            return Map.of("status", "ok");
        });
    }

    @GetMapping(AdapterScenarioPaths.OBSERVABILITY_TRACE)
    public Map<String, String> observabilityTrace() {
        return Map.of("status", "ok");
    }

    @GetMapping(AdapterScenarioPaths.DOWNLOAD)
    public DownloadResource download() {
        return AdapterDownloadFixtures.memoryDownload();
    }

    private <T> T onBoundedElasticBlocking(ServiceContext serviceContext, Supplier<T> supplier) {
        return onBoundedElastic(serviceContext, supplier).block();
    }

    private <T> Mono<T> onBoundedElastic(ServiceContext serviceContext, Supplier<T> supplier) {
        ServerWebExchange exchange = ServiceReactiveExchangeHolder.get();
        return Mono.fromCallable(() -> {
            if (exchange != null) {
                ServiceReactiveExchangeHolder.set(exchange);
            }
            ContextSnapshot snapshot = contexts.install(serviceContext);
            try {
                return supplier.get();
            } finally {
                contexts.restore(snapshot);
                if (exchange != null) {
                    ServiceReactiveExchangeHolder.clear();
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
