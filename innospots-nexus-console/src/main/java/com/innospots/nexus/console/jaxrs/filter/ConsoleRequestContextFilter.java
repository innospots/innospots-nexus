package com.innospots.nexus.console.jaxrs.filter;

import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.jaxrs.support.ConsoleTokenSessionBinder;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;
import com.innospots.nexus.core.jaxrs.support.RequestScope;

/**
 * 分配 requestId，并在请求开始与响应结束后清理线程上下文。
 *
 * <p><b>为什么必须在鉴权过滤器之前执行：</b>JAX-RS 只执行「已经运行过」的请求过滤器的响应阶段。
 * 若本过滤器排在鉴权之后，认证/授权过滤器抛出 401/403 时它的响应阶段不会执行，
 * 线程池会残留上一次请求的 {@code SessionContext} / {@code TLC} 身份，
 * 可能被后续匿名请求（如公共页面访问校验）误用。把它提前到最前，并在请求开始时清理，
 * 可以保证每个请求都从干净上下文开始，且异常路径也能完成清理。</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 200)
public final class ConsoleRequestContextFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // 清理上一次请求可能残留的身份与作用域（异常路径下响应阶段可能不会执行）。
        ConsoleTokenSessionBinder.clear();
        RequestScope.bind(requestContext);
        String requestId = requestContext.getHeaderString(HttpHeaderNames.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().replace("-", "");
        }
        requestContext.setProperty(RequestProperties.REQUEST_ID, requestId);
        TLC.put(TLC.TRACE_ID, requestId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object requestId = requestContext.getProperty(RequestProperties.REQUEST_ID);
        if (requestId != null) {
            responseContext.getHeaders().putSingle(HttpHeaderNames.REQUEST_ID, String.valueOf(requestId));
        }
        ConsoleTokenSessionBinder.clear();
        RequestScope.clear();
    }
}
