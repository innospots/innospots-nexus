package com.innospots.nexus.console.jaxrs.support;

import jakarta.ws.rs.container.ContainerRequestContext;

/**
 * 当前 JAX-RS 请求的线程绑定，供 {@link jakarta.ws.rs.ext.ExceptionMapper} 读取请求元数据。
 */
public final class ConsoleJaxRsRequestScope {

    private static final ThreadLocal<ContainerRequestContext> CURRENT = new ThreadLocal<>();

    private ConsoleJaxRsRequestScope() {
    }

    /**
     * 在请求入口绑定上下文。
     *
     * @param requestContext 当前请求
     */
    public static void bind(ContainerRequestContext requestContext) {
        CURRENT.set(requestContext);
    }

    /**
     * 返回当前请求上下文；无绑定时为 {@code null}。
     *
     * @return 请求上下文
     */
    public static ContainerRequestContext current() {
        return CURRENT.get();
    }

    /**
     * 请求结束时清理绑定。
     */
    public static void clear() {
        CURRENT.remove();
    }
}
