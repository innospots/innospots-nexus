package com.innospots.nexus.service.adapter.test.scenario;

import java.util.Map;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 匿名拒绝与授权通过。
 */
public final class HttpSecurityScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange anonymous = client.get(target, AdapterScenarioPaths.SECURE);
        require(anonymous.status() == 403, "anonymous should be forbidden");

        AdapterTestTarget authorized = new AdapterTestTarget(
                target.baseUrl(),
                merge(target.defaultHeaders(), Map.of(StandardHeaders.AUTHORIZATION, "Bearer adapter-user")));
        HttpTestClient.HttpExchange allowed = client.get(authorized, AdapterScenarioPaths.SECURE);
        require(allowed.status() == 200, "authorized request should succeed");
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
