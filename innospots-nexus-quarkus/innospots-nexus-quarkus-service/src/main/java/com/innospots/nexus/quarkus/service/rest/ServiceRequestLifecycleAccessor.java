package com.innospots.nexus.quarkus.service.rest;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * 从 JAX-RS 请求上下文读取 {@link ServiceRequestLifecycle}。
 */
@RequestScoped
public final class ServiceRequestLifecycleAccessor {

    private final ContainerRequestContext requestContext;

    public ServiceRequestLifecycleAccessor(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * 返回当前请求生命周期。
     *
     * @return 生命周期
     */
    public ServiceRequestLifecycle requireCurrent() {
        Object value = requestContext.getProperty(ServiceRequestLifecycle.REQUEST_PROPERTY);
        if (!(value instanceof ServiceRequestLifecycle lifecycle)) {
            throw NexusException.build(ServiceStatusCode.CONTEXT_UNAVAILABLE);
        }
        return lifecycle;
    }
}
