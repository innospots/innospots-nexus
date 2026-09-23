package com.innospots.nexus.service.adapter.test.scenario;

/**
 * 夹具路径常量。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class AdapterScenarioPaths {

    public static final String UNAUGMENTED = "/adapter/unaugmented";
    public static final String CONTEXT = "/adapter/context";
    public static final String ERROR_UNAUTHORIZED = "/adapter/errors/unauthorized";
    public static final String ERROR_FORBIDDEN = "/adapter/errors/forbidden";
    public static final String ERROR_NOT_FOUND = "/adapter/errors/not-found";
    public static final String ERROR_RATE_LIMIT = "/adapter/errors/rate-limit";
    public static final String ERROR_INTERNAL = "/adapter/errors/internal";
    public static final String SECURE = "/adapter/secure";
    public static final String SLOW = "/adapter/slow";
    public static final String STREAM_SSE = "/adapter/stream/sse";
    public static final String STREAM_CANCEL = "/adapter/stream/cancel";
    public static final String WEBSOCKET = "/adapter/ws";
    public static final String GOVERNANCE_RATE_LIMIT = "/adapter/governance/rate-limit";
    public static final String GOVERNANCE_TIMEOUT = "/adapter/governance/timeout";
    public static final String OBSERVABILITY_TRACE = "/adapter/observability/trace";
    public static final String DOWNLOAD = "/adapter/download";

    private AdapterScenarioPaths() {
    }
}
