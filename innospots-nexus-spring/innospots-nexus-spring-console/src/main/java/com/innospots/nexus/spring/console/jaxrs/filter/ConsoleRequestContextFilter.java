package com.innospots.nexus.spring.console.jaxrs.filter;

import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleHttpHeaders;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleTokenSessionBinder;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleWebRequestProperties;

/**
 * 分配 requestId 并在响应结束后清理线程上下文。
 */
@Provider
@Priority(Priorities.USER)
public final class ConsoleRequestContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String requestId = requestContext.getHeaderString(ConsoleHttpHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        requestContext.setProperty(ConsoleWebRequestProperties.REQUEST_ID, requestId);
        TLC.put(TLC.TRACE_ID, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object requestId = requestContext.getProperty(ConsoleWebRequestProperties.REQUEST_ID);
        if (requestId != null) {
            responseContext.getHeaders().putSingle(ConsoleHttpHeaders.REQUEST_ID, String.valueOf(requestId));
        }
        ConsoleTokenSessionBinder.clear();
    }
}
