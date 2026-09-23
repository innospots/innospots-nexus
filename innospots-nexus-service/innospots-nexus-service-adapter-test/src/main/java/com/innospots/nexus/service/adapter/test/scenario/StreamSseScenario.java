package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * SSE Content-Type 与事件帧。
 */
public final class StreamSseScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange exchange = client.get(target, AdapterScenarioPaths.STREAM_SSE);
        require(exchange.status() == 200, "expected 200");
        String contentType = exchange.firstHeader(StandardHeaders.CONTENT_TYPE).orElse("");
        require(contentType.startsWith("text/event-stream"), "expected event stream content type");
        require(exchange.body().contains("event:"), "expected SSE event frame");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
