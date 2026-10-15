package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/**
 * 未捕获异常兜底映射。
 */
@Provider
public final class ThrowableExceptionMapper implements ExceptionMapper<Throwable> {

    private final JaxRsExceptionSupport exceptionSupport;

    public ThrowableExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        this.exceptionSupport = exceptionSupport;
    }

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException webApplicationException) {
            return exceptionSupport.toWebApplicationResponse(webApplicationException);
        }
        NexusException nexusException = resolveNexusException(exception);
        if (nexusException != null) {
            exceptionSupport.logNexusFailure(nexusException);
            return exceptionSupport.toResponse(nexusException);
        }
        exceptionSupport.logUnhandledFailure(exception);
        return exceptionSupport.toResponse(NexusException.build(NexusStatusCode.SYSTEM_ERROR, exception));
    }

    private static NexusException resolveNexusException(Throwable failure) {
        if (failure instanceof NexusException nexusException) {
            return nexusException;
        }
        Throwable current = failure;
        while (current != null) {
            if (current instanceof NexusException nexusException) {
                return nexusException;
            }
            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
        return null;
    }
}
