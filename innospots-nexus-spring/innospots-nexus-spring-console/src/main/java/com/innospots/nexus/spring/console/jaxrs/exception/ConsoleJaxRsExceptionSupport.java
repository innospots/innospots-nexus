package com.innospots.nexus.spring.console.jaxrs.exception;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleHttpHeaders;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleWebRequestProperties;

/**
 * 将 {@link NexusException} 转为 JAX-RS {@link Response}（legacy {@link R} 形态）。
 */
public final class ConsoleJaxRsExceptionSupport {

    public Response toResponse(NexusException exception) {
        String requestId = resolveRequestId(null);
        int httpStatus = resolveHttpStatus(exception);
        R<Void> body = R.fail(exception.code(), exception.getMessage(), exception.display());
        return Response.status(httpStatus)
                .header(ConsoleHttpHeaders.REQUEST_ID, requestId)
                .entity(body)
                .build();
    }

    public Response toResponse(ContainerRequestContext requestContext, NexusException exception) {
        String requestId = resolveRequestId(requestContext);
        int httpStatus = resolveHttpStatus(exception);
        R<Void> body = R.fail(exception.code(), exception.getMessage(), exception.display());
        return Response.status(httpStatus)
                .header(ConsoleHttpHeaders.REQUEST_ID, requestId)
                .entity(body)
                .build();
    }

    private static int resolveHttpStatus(NexusException exception) {
        return NexusStatusCode.findByFullCode(exception.code())
                .map(NexusStatusCode::httpStatusCode)
                .orElse(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
    }

    private static String resolveRequestId(ContainerRequestContext requestContext) {
        if (requestContext != null) {
            Object value = requestContext.getProperty(ConsoleWebRequestProperties.REQUEST_ID);
            if (value != null) {
                String requestId = String.valueOf(value);
                if (!requestId.isBlank()) {
                    return requestId;
                }
            }
        }
        String traceId = TLC.getString(TLC.TRACE_ID);
        if (traceId != null && !traceId.isBlank()) {
            return traceId;
        }
        return "unknown";
    }
}
