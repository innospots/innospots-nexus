package com.innospots.nexus.quarkus.service.config;

import java.time.Duration;
import java.util.Map;

import com.innospots.nexus.quarkus.service.governance.DeadlineOperationTimeoutArmer;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.governance.bulkhead.BulkheadInterceptor;
import com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider;
import com.innospots.nexus.service.governance.circuit.CircuitBreakerInterceptor;
import com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;
import com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider;
import com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor;
import com.innospots.nexus.service.governance.timeout.TimeoutInterceptor;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.lifecycle.ServiceRuntime;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * 持有已启动的 {@link ServiceRuntime}。
 */
@ApplicationScoped
public final class ServiceRuntimeHolder {

    private static final GovernanceConfig GOVERNANCE_CONFIG = new GovernanceConfig(
            Map.of(
                    "adapter-rate-limit", new RateLimitPolicy(1, 1.0D),
                    "adapter-ws-rate-limit", new RateLimitPolicy(1, 1.0D)),
            Map.of(),
            Map.of(),
            Map.of("adapter.governance.timeout", Duration.ofMillis(200)),
            10_000,
            Duration.ofMinutes(15),
            Duration.ofSeconds(30),
            Duration.ofMinutes(60),
            Duration.ofSeconds(30));

    @Inject
    Instance<SecurityProvider> securityProvider;

    @Inject
    Instance<PermissionProvider> permissionProvider;

    @Inject
    DeadlineOperationTimeoutArmer deadlineOperationTimeoutArmer;

    private ServiceRuntime serviceRuntime;

    @PostConstruct
    void start() {
        ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
        RateLimitInterceptor rateLimitInterceptor = new RateLimitInterceptor(
                new LocalTokenBucketProvider(GOVERNANCE_CONFIG, Ticker.system()),
                GOVERNANCE_CONFIG);
        BulkheadInterceptor bulkheadInterceptor = new BulkheadInterceptor(
                new SemaphoreBulkheadProvider(GOVERNANCE_CONFIG));
        CircuitBreakerInterceptor circuitBreakerInterceptor = new CircuitBreakerInterceptor(
                new Resilience4jCircuitBreakerProvider(GOVERNANCE_CONFIG));
        TimeoutInterceptor timeoutInterceptor = new TimeoutInterceptor(GOVERNANCE_CONFIG, deadlineOperationTimeoutArmer);
        ServiceRuntime.Builder builder = ServiceRuntime.builder()
                .contexts(contexts)
                .addInterceptor(rateLimitInterceptor)
                .addInterceptor(bulkheadInterceptor)
                .addInterceptor(circuitBreakerInterceptor)
                .addInterceptor(timeoutInterceptor);
        if (securityProvider.isResolvable()) {
            builder.securityProvider(securityProvider.get());
        }
        if (permissionProvider.isResolvable()) {
            builder.permissionProvider(permissionProvider.get());
        }
        serviceRuntime = builder.build();
        serviceRuntime.start();
    }

    @PreDestroy
    void shutdown() {
        if (serviceRuntime != null) {
            serviceRuntime.close();
        }
    }

    public ServiceRuntime serviceRuntime() {
        return serviceRuntime;
    }

    public InvocationEngine invocationEngine() {
        return serviceRuntime.engine();
    }

    public ThreadBoundServiceContext contexts() {
        return serviceRuntime.contexts();
    }

    public GovernanceConfig governanceConfig() {
        return GOVERNANCE_CONFIG;
    }
}
