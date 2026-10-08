package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;

/**
 * 将 {@link NexusException} 映射为统一 HTTP 错误响应。
 */
@Provider
public final class NexusExceptionMapper implements ExceptionMapper<NexusException> {

    private final JaxRsExceptionSupport exceptionSupport;

    public NexusExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(NexusException exception) {
        exceptionSupport.logNexusFailure(exception);
        return exceptionSupport.toResponse(exception);
    }
}
