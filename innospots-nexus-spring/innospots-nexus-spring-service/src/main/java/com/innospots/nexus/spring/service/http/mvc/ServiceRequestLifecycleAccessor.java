package com.innospots.nexus.spring.service.http.mvc;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.spring.service.http.webflux.ServiceReactiveExchangeHolder;

/**
 * 从 Spring 请求上下文读取 {@link ServiceRequestLifecycle}。
 */
public final class ServiceRequestLifecycleAccessor {

    /**
     * 返回当前请求生命周期。
     *
     * @return 生命周期
     */
    public ServiceRequestLifecycle requireCurrent() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            Object value = attributes.getAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if (value instanceof ServiceRequestLifecycle lifecycle) {
                return lifecycle;
            }
        }
        ServerWebExchange exchange = ServiceReactiveExchangeHolder.get();
        if (exchange != null) {
            Object value = exchange.getAttribute(ServiceRequestLifecycle.REQUEST_ATTRIBUTE);
            if (value instanceof ServiceRequestLifecycle lifecycle) {
                return lifecycle;
            }
        }
        throw NexusException.build(ServiceStatusCode.CONTEXT_UNAVAILABLE);
    }
}
