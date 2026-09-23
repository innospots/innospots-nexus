package com.innospots.nexus.spring.service.core;

import jakarta.annotation.PreDestroy;

import com.innospots.nexus.service.runtime.lifecycle.ServiceRuntime;

/**
 * 在 Spring 容器销毁时停止 {@link ServiceRuntime}，避免审计与拦截器链泄漏。
 *
 * @author Smars
 * @date 2026/09/19
 * @see ServiceRuntime
 * @see ServiceCoreConfiguration
 */
final class ServiceRuntimeShutdown {

    private final ServiceRuntime runtime;

    ServiceRuntimeShutdown(ServiceRuntime runtime) {
        this.runtime = runtime;
    }

    @PreDestroy
    void shutdown() {
        runtime.close().toCompletableFuture().join();
    }
}
