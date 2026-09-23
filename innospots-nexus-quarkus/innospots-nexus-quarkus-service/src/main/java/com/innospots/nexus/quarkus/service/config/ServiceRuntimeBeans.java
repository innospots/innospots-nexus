package com.innospots.nexus.quarkus.service.config;

import com.innospots.nexus.service.http.error.HttpErrorMapper;
import com.innospots.nexus.service.http.header.RequestIdPolicy;
import com.innospots.nexus.service.observability.config.ObservabilityConfig;
import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.observability.logging.SensitiveValueMasker;
import com.innospots.nexus.service.observability.trace.NoOpTraceProvider;
import com.innospots.nexus.service.contract.trace.TraceProvider;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * 默认 CDI bean 生产者。
 */
@ApplicationScoped
public final class ServiceRuntimeBeans {

    @Inject
    ServiceRuntimeHolder serviceRuntimeHolder;

    @Produces
    @Singleton
    @DefaultBean
    RequestIdPolicy requestIdPolicy() {
        return new RequestIdPolicy();
    }

    @Produces
    @Singleton
    @DefaultBean
    HttpErrorMapper httpErrorMapper() {
        return new HttpErrorMapper();
    }

    @Produces
    @Singleton
    @DefaultBean
    AnnotationPolicyResolver annotationPolicyResolver() {
        return new AnnotationPolicyResolver();
    }

    @Produces
    @Singleton
    @DefaultBean
    MdcContextBridge mdcContextBridge() {
        return new MdcContextBridge();
    }

    @Produces
    @Singleton
    @DefaultBean
    SensitiveValueMasker sensitiveValueMasker() {
        return new SensitiveValueMasker();
    }

    @Produces
    @Singleton
    @DefaultBean
    ObservabilityConfig observabilityConfig() {
        return ObservabilityConfig.defaults();
    }

    @Produces
    @Singleton
    @DefaultBean
    AccessLogWriter accessLogWriter(ObservabilityConfig observabilityConfig, SensitiveValueMasker sensitiveValueMasker) {
        return new AccessLogWriter(observabilityConfig, sensitiveValueMasker);
    }

    @Produces
    @Singleton
    @DefaultBean
    TraceProvider traceProvider() {
        return new NoOpTraceProvider();
    }

    @Produces
    @Singleton
    InvocationEngine invocationEngine() {
        return serviceRuntimeHolder.invocationEngine();
    }
}
