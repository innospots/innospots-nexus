package com.innospots.nexus.console.jaxrs.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * 保留 JAX-RS 原生 HTTP 状态（如 404），避免被 {@link ConsoleThrowableExceptionMapper} 误映射为系统错误。
 */
@Provider
public final class ConsoleWebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private final ConsoleJaxRsExceptionSupport exceptionSupport;

    public ConsoleWebApplicationExceptionMapper(ConsoleJaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        return exceptionSupport.toWebApplicationResponse(exception);
    }
}
