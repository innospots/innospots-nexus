package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * 保留 JAX-RS 原生 HTTP 状态（如 404），避免被 {@link ThrowableExceptionMapper} 误映射为系统错误。
 */
@Provider
public final class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private final JaxRsExceptionSupport exceptionSupport;

    public WebApplicationExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(WebApplicationException exception) {
        return exceptionSupport.toWebApplicationResponse(exception);
    }
}
