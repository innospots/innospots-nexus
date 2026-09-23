package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 无框架注解 HTTP 仍具备 requestId 与基础响应。
 */
public final class HttpUnaugmentedScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange exchange = client.get(target, AdapterScenarioPaths.UNAUGMENTED);
        require(exchange.status() == 200, "expected 200");
        require(exchange.firstHeader(StandardHeaders.REQUEST_ID).filter(value -> !value.isBlank()).isPresent(),
                "missing request id header");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
