package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 429 与 Retry-After 头。
 */
public final class GovernanceRateLimitScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange first = client.get(target, AdapterScenarioPaths.GOVERNANCE_RATE_LIMIT);
        HttpTestClient.HttpExchange second = client.get(target, AdapterScenarioPaths.GOVERNANCE_RATE_LIMIT);
        require(second.status() == 429, "expected 429 on repeated request");
        require(second.firstHeader(StandardHeaders.RETRY_AFTER).filter(value -> !value.isBlank()).isPresent(),
                "missing Retry-After");
        require(first.status() == 200, "first request should succeed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
