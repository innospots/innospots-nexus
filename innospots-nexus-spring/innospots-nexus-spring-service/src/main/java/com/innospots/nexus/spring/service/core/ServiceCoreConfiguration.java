package com.innospots.nexus.spring.service.core;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.service.contract.governance.RateLimitProvider;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.contract.trace.TraceProvider;
import com.innospots.nexus.service.governance.bulkhead.BulkheadInterceptor;
import com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider;
import com.innospots.nexus.service.governance.circuit.CircuitBreakerInterceptor;
import com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;
import com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider;
import com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor;
import com.innospots.nexus.service.governance.timeout.TimeoutInterceptor;
import com.innospots.nexus.service.http.error.HttpErrorMapper;
import com.innospots.nexus.service.http.header.RequestIdPolicy;
import com.innospots.nexus.service.observability.config.ObservabilityConfig;
import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.observability.logging.SensitiveValueMasker;
import com.innospots.nexus.service.observability.trace.NoOpTraceProvider;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.lifecycle.ServiceRuntime;
import com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver;
import com.innospots.nexus.service.runtime.time.DeadlineScheduler;
import com.innospots.nexus.spring.service.governance.redis.ServiceGovernanceRateLimitRedisConfiguration;
import com.innospots.nexus.spring.service.http.governance.DeadlineOperationTimeoutArmer;
import com.innospots.nexus.spring.service.http.governance.ServiceGovernanceAspectConfiguration;
import com.innospots.nexus.spring.service.http.invocation.GovernedInvocationExecutor;
import com.innospots.nexus.spring.service.http.invocation.ServiceInvocationBridge;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycleAccessor;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;

/**
 * Spring adapter 共享运行时 Bean。
 *
 * @author Smars
 * @date 2026/09/16
 * @see ServiceDeadlineScheduledExecutor
 */
@Configuration
@org.springframework.context.annotation.Import({
        ServiceGovernanceAspectConfiguration.class,
        ServiceGovernanceRateLimitRedisConfiguration.class
})
public class ServiceCoreConfiguration {

    @Bean
    ThreadBoundServiceContext serviceThreadBoundServiceContext() {
        return new ThreadBoundServiceContext();
    }

    @Bean
    RequestIdPolicy serviceRequestIdPolicy() {
        return new RequestIdPolicy();
    }

    @Bean
    HttpErrorMapper serviceHttpErrorMapper() {
        return new HttpErrorMapper();
    }

    @Bean
    AnnotationPolicyResolver serviceAnnotationPolicyResolver() {
        return new AnnotationPolicyResolver();
    }

    @Bean
    ServiceRequestLifecycleAccessor serviceRequestLifecycleAccessor() {
        return new ServiceRequestLifecycleAccessor();
    }

    @Bean
    ServiceTransportSupport serviceTransportSupport(RequestIdPolicy requestIdPolicy) {
        return new ServiceTransportSupport(requestIdPolicy);
    }

    @Bean
    MdcContextBridge serviceMdcContextBridge() {
        return new MdcContextBridge();
    }

    @Bean
    SensitiveValueMasker serviceSensitiveValueMasker() {
        return new SensitiveValueMasker();
    }

    @Bean
    @ConditionalOnMissingBean
    ObservabilityConfig serviceObservabilityConfig() {
        return ObservabilityConfig.defaults();
    }

    @Bean
    AccessLogWriter serviceAccessLogWriter(ObservabilityConfig config, SensitiveValueMasker masker) {
        return new AccessLogWriter(config, masker);
    }

    @Bean
    @ConditionalOnMissingBean
    TraceProvider serviceTraceProvider() {
        return new NoOpTraceProvider();
    }

    @Bean(destroyMethod = "close")
    ServiceDeadlineScheduledExecutor serviceDeadlineScheduledExecutor() {
        return new ServiceDeadlineScheduledExecutor();
    }

    @Bean
    DeadlineScheduler serviceDeadlineScheduler(ServiceDeadlineScheduledExecutor serviceDeadlineScheduledExecutor) {
        return new DeadlineScheduler(serviceDeadlineScheduledExecutor.executor());
    }

    @Bean
    @ConditionalOnMissingBean
    GovernanceConfig serviceGovernanceConfig() {
        return defaultGovernanceConfig();
    }

    @Bean
    @ConditionalOnProperty(prefix = "service.governance.rate-limit", name = "store", havingValue = "local", matchIfMissing = true)
    @ConditionalOnMissingBean(RateLimitProvider.class)
    LocalTokenBucketProvider serviceLocalRateLimitProvider(GovernanceConfig serviceGovernanceConfig) {
        return new LocalTokenBucketProvider(serviceGovernanceConfig, Ticker.system());
    }

    @Bean
    RateLimitInterceptor serviceRateLimitInterceptor(
            RateLimitProvider serviceRateLimitProvider,
            GovernanceConfig serviceGovernanceConfig) {
        return new RateLimitInterceptor(serviceRateLimitProvider, serviceGovernanceConfig);
    }

    @Bean
    SemaphoreBulkheadProvider serviceBulkheadProvider(GovernanceConfig serviceGovernanceConfig) {
        return new SemaphoreBulkheadProvider(serviceGovernanceConfig);
    }

    @Bean
    BulkheadInterceptor serviceBulkheadInterceptor(SemaphoreBulkheadProvider serviceBulkheadProvider) {
        return new BulkheadInterceptor(serviceBulkheadProvider);
    }

    @Bean
    Resilience4jCircuitBreakerProvider serviceCircuitBreakerProvider(GovernanceConfig serviceGovernanceConfig) {
        return new Resilience4jCircuitBreakerProvider(serviceGovernanceConfig);
    }

    @Bean
    CircuitBreakerInterceptor serviceCircuitBreakerInterceptor(
            Resilience4jCircuitBreakerProvider serviceCircuitBreakerProvider) {
        return new CircuitBreakerInterceptor(serviceCircuitBreakerProvider);
    }

    @Bean
    TimeoutInterceptor serviceTimeoutInterceptor(
            GovernanceConfig serviceGovernanceConfig,
            DeadlineOperationTimeoutArmer deadlineOperationTimeoutArmer) {
        return new TimeoutInterceptor(serviceGovernanceConfig, deadlineOperationTimeoutArmer);
    }

    @Bean
    DeadlineOperationTimeoutArmer deadlineOperationTimeoutArmer(
            DeadlineScheduler serviceDeadlineScheduler,
            ServiceRequestLifecycleAccessor serviceRequestLifecycleAccessor) {
        return new DeadlineOperationTimeoutArmer(serviceDeadlineScheduler, serviceRequestLifecycleAccessor);
    }

    @Bean
    ServiceRuntime serviceRuntime(
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            ObjectProvider<SecurityProvider> securityProvider,
            ObjectProvider<PermissionProvider> permissionProvider,
            RateLimitInterceptor serviceRateLimitInterceptor,
            BulkheadInterceptor serviceBulkheadInterceptor,
            CircuitBreakerInterceptor serviceCircuitBreakerInterceptor,
            TimeoutInterceptor serviceTimeoutInterceptor) {
        ServiceRuntime.Builder builder = ServiceRuntime.builder()
                .contexts(serviceThreadBoundServiceContext)
                .addInterceptor(serviceRateLimitInterceptor)
                .addInterceptor(serviceBulkheadInterceptor)
                .addInterceptor(serviceCircuitBreakerInterceptor)
                .addInterceptor(serviceTimeoutInterceptor);
        securityProvider.ifAvailable(builder::securityProvider);
        permissionProvider.ifAvailable(builder::permissionProvider);
        ServiceRuntime runtime = builder.build();
        runtime.start();
        return runtime;
    }

    @Bean
    ServiceRuntimeShutdown serviceRuntimeShutdown(ServiceRuntime serviceRuntime) {
        return new ServiceRuntimeShutdown(serviceRuntime);
    }

    @Bean
    InvocationEngine serviceInvocationEngine(ServiceRuntime serviceRuntime) {
        return serviceRuntime.engine();
    }

    @Bean
    GovernedInvocationExecutor governedInvocationExecutor(
            InvocationEngine serviceInvocationEngine,
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            AnnotationPolicyResolver serviceAnnotationPolicyResolver) {
        return new GovernedInvocationExecutor(
                serviceInvocationEngine,
                serviceThreadBoundServiceContext,
                serviceAnnotationPolicyResolver);
    }

    @Bean
    ServiceInvocationBridge serviceInvocationBridge(GovernedInvocationExecutor governedInvocationExecutor) {
        return new ServiceInvocationBridge(governedInvocationExecutor);
    }

    private static GovernanceConfig defaultGovernanceConfig() {
        return new GovernanceConfig(
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
    }
}
