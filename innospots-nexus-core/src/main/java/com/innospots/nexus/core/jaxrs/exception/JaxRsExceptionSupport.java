package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.status.StatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;
import com.innospots.nexus.core.jaxrs.support.RequestScope;

/**
 * 将 {@link NexusException} 转为 JAX-RS {@link Response}（legacy {@link R} 形态）。
 */
public final class JaxRsExceptionSupport {

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsExceptionSupport.class);

    /**
     * 记录 JAX-RS {@link WebApplicationException}（含路由未匹配的 {@link jakarta.ws.rs.NotFoundException}）。
     *
     * @param exception Web 应用异常
     */
    public void logWebApplicationFailure(WebApplicationException exception) {
        ContainerRequestContext requestContext = RequestScope.current();
        String requestId = resolveRequestId(requestContext);
        String operation = resolveOperation(requestContext);
        Response response = exception.getResponse();
        int status = response == null ? 500 : response.getStatus();
        if (status >= 500) {
            LOG.error(
                    "JAX-RS web application failure requestId={} operation={} httpStatus={} message={}",
                    requestId,
                    operation,
                    status,
                    exception.getMessage(),
                    exception);
            return;
        }
        LOG.warn(
                "JAX-RS web application failure requestId={} operation={} httpStatus={} message={}",
                requestId,
                operation,
                status,
                exception.getMessage());
    }

    /**
     * 将 {@link WebApplicationException} 转为响应（不包装为 {@link com.innospots.nexus.base.domain.response.R}）。
     *
     * @param exception Web 应用异常
     * @return 原生响应或 500 兜底
     */
    public Response toWebApplicationResponse(WebApplicationException exception) {
        logWebApplicationFailure(exception);
        Response response = exception.getResponse();
        if (response != null) {
            return response;
        }
        return Response.serverError().build();
    }

    /**
     * 记录未捕获异常（映射为 {@link NexusStatusCode#SYSTEM_ERROR} 前），含完整堆栈。
     *
     * @param failure 原始失败
     */
    public void logUnhandledFailure(Throwable failure) {
        ContainerRequestContext requestContext = RequestScope.current();
        String requestId = resolveRequestId(requestContext);
        String operation = resolveOperation(requestContext);
        LOG.error(
                "JAX-RS unhandled failure requestId={} operation={} exceptionType={} message={}",
                requestId,
                operation,
                failure.getClass().getName(),
                failure.getMessage(),
                failure);
    }

    /**
     * 记录 {@link NexusException}；5xx 或存在 cause 时输出 ERROR 与堆栈，其余为 WARN。
     *
     * @param exception 平台异常
     */
    public void logNexusFailure(NexusException exception) {
        ContainerRequestContext requestContext = RequestScope.current();
        String requestId = resolveRequestId(requestContext);
        String operation = resolveOperation(requestContext);
        int httpStatus = resolveHttpStatus(exception);
        Throwable cause = exception.getCause();
        if (httpStatus >= 500 || cause != null) {
            Throwable logged = cause != null ? cause : exception;
            LOG.error(
                    "JAX-RS failure requestId={} operation={} httpStatus={} code={} message={}",
                    requestId,
                    operation,
                    httpStatus,
                    exception.code(),
                    exception.getMessage(),
                    logged);
            return;
        }
        LOG.warn(
                "JAX-RS rejected requestId={} operation={} httpStatus={} code={} message={}",
                requestId,
                operation,
                httpStatus,
                exception.code(),
                exception.getMessage());
    }

    /**
     * 将 {@link NexusException} 转为当前请求作用域下的响应。
     *
     * @param exception 平台异常
     * @return 统一错误响应
     */
    public Response toResponse(NexusException exception) {
        return toResponse(RequestScope.current(), exception);
    }

    /**
     * 将 {@link NexusException} 转为携带 requestId 头的统一错误响应。
     *
     * @param requestContext 当前请求（可为 {@code null}）
     * @param exception 平台异常
     * @return 统一错误响应
     */
    public Response toResponse(ContainerRequestContext requestContext, NexusException exception) {
        String requestId = resolveRequestId(requestContext);
        int httpStatus = resolveHttpStatus(exception);
        R<Void> body = R.fail(exception.code(), exception.getMessage(), exception.display());
        return Response.status(httpStatus)
                .type(MediaType.APPLICATION_JSON)
                .header(HttpHeaderNames.REQUEST_ID, requestId)
                .entity(body)
                .build();
    }

    private static int resolveHttpStatus(NexusException exception) {
        // 优先使用异常构建时携带的状态码：产品域状态码（非 AIO* 平台码）
        // 也能按自身声明返回 HTTP 状态
        StatusCode statusCode = exception.statusCode();
        if (statusCode != null) {
            return statusCode.httpStatusCode();
        }
        // 兼容仅携带原始错误码的异常：按平台码解析，未知码兜底 500
        return NexusStatusCode.findByFullCode(exception.code())
                .map(NexusStatusCode::httpStatusCode)
                .orElse(NexusStatusCode.SYSTEM_ERROR.httpStatusCode());
    }

    private static String resolveRequestId(ContainerRequestContext requestContext) {
        if (requestContext != null) {
            Object value = requestContext.getProperty(RequestProperties.REQUEST_ID);
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

    private static String resolveOperation(ContainerRequestContext requestContext) {
        if (requestContext == null) {
            return "unknown";
        }
        return requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri();
    }
}
