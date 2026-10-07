package com.innospots.nexus.service.adapter.test.scenario;

import java.util.Map;
import java.util.UUID;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * requestId 透传与上下文回显。
 */
public final class HttpContextScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        String requestId = "req-" + UUID.randomUUID();
        AdapterTestTarget withHeader = new AdapterTestTarget(
                target.baseUrl(),
                merge(target.defaultHeaders(), Map.of(StandardHeaders.REQUEST_ID, requestId)));
        HttpTestClient.HttpExchange exchange = client.get(withHeader, AdapterScenarioPaths.CONTEXT);
        require(exchange.status() == 200, "expected 200");
        Map<String, Object> body = Jsons.toMap(exchange.body());
        require(requestId.equals(body.get("requestId")), "context requestId mismatch");
        require(exchange.firstHeader(StandardHeaders.REQUEST_ID).orElse("").equals(requestId),
                "response request id mismatch");
    }

    private static Map<String, String> merge(Map<String, String> left, Map<String, String> right) {
        java.util.LinkedHashMap<String, String> merged = new java.util.LinkedHashMap<>(left);
        merged.putAll(right);
        return Map.copyOf(merged);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
