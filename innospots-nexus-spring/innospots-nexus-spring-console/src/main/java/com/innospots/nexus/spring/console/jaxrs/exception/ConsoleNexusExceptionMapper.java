package com.innospots.nexus.spring.console.jaxrs.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;

/**
 * 将 {@link NexusException} 映射为统一 HTTP 错误响应。
 */
@Provider
public final class ConsoleNexusExceptionMapper implements ExceptionMapper<NexusException> {

    private final ConsoleJaxRsExceptionSupport exceptionSupport;

    public ConsoleNexusExceptionMapper(ConsoleJaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(NexusException exception) {
        return exceptionSupport.toResponse(exception);
    }
}
