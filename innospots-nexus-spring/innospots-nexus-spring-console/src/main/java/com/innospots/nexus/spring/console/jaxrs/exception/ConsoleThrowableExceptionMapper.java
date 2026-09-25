package com.innospots.nexus.spring.console.jaxrs.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 未捕获异常兜底映射。
 */
@Provider
public final class ConsoleThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private final ConsoleJaxRsExceptionSupport exceptionSupport;

    public ConsoleThrowableExceptionMapper(ConsoleJaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(Throwable exception) {
        NexusException nexusException = NexusException.build(NexusStatusCode.SYSTEM_ERROR, exception);
        return exceptionSupport.toResponse(nexusException);
    }
}
