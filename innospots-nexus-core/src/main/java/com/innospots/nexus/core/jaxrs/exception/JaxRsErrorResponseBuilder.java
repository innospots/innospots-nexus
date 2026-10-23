package com.innospots.nexus.core.jaxrs.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.jaxrs.support.HttpHeaderNames;

/**
 * 构建 JAX-RS 错误响应；生产默认实现委托 {@link Response}，单测可替换以避免 JAX-RS 运行时。
 */
@FunctionalInterface
interface JaxRsErrorResponseBuilder {

    JaxRsErrorResponseBuilder JAKARTA = (httpStatus, body, requestId) -> Response.status(httpStatus)
            .type(MediaType.APPLICATION_JSON)
            .header(HttpHeaderNames.REQUEST_ID, requestId)
            .entity(body)
            .build();

    /**
     * 构建携带 requestId 与 legacy {@link R} 实体的错误响应。
     */
    Response build(int httpStatus, R<Void> body, String requestId);

    /**
     * 无更具体响应时的 500 兜底。
     */
    default Response serverError() {
        return Response.serverError().build();
    }
}
