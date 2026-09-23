package com.innospots.nexus.service.adapter.test.scenario;

import java.time.Duration;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 方法超时映射为 SRV100002 / 504。
 */
public final class GovernanceTimeoutScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange exchange = client.get(
                target,
                AdapterScenarioPaths.GOVERNANCE_TIMEOUT,
                Duration.ofSeconds(5));
        require(exchange.status() == 504, "expected 504");
        if (!exchange.body().isBlank()) {
            require(ServiceStatusCode.DEADLINE_EXCEEDED.fullCode().equals(Jsons.toMap(exchange.body()).get("code")),
                    "expected deadline exceeded code");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
