package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 响应或头中包含合法 trace 关联；无 SDK 时不造假全零 traceId。
 */
public final class ObservabilityTraceScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange exchange = client.get(target, AdapterScenarioPaths.OBSERVABILITY_TRACE);
        require(exchange.status() == 200, "expected 200");
        String requestId = exchange.firstHeader(StandardHeaders.REQUEST_ID).orElse("");
        require(!requestId.isBlank(), "missing request id");
        String traceParent = exchange.firstHeader(StandardHeaders.TRACE_PARENT).orElse("");
        if (!traceParent.isBlank()) {
            require(!traceParent.startsWith("00-00000000000000000000000000000000-"), "forbidden zero trace id");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
