package com.innospots.nexus.service.http.header;

/**
 * 服务框架使用的标准 HTTP 头名称。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class StandardHeaders {

    public static final String REQUEST_ID = "X-Request-Id";
    public static final String CLIENT_ID = "X-Client-Id";
    public static final String TRACE_PARENT = "traceparent";
    public static final String TRACE_STATE = "tracestate";
    public static final String BAGGAGE = "baggage";
    public static final String RETRY_AFTER = "Retry-After";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private StandardHeaders() {
    }
}
