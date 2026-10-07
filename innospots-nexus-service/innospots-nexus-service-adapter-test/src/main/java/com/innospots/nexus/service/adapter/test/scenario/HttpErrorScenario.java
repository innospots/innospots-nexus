package com.innospots.nexus.service.adapter.test.scenario;

import java.util.Map;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.http.header.StandardHeaders;

/**
 * 401/403/404/429/500 错误形状一致性。
 */
public final class HttpErrorScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        expectStatus(target, AdapterScenarioPaths.ERROR_UNAUTHORIZED, 401, NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
        expectStatus(target, AdapterScenarioPaths.ERROR_FORBIDDEN, 403, NexusStatusCode.NO_PERMISSION.fullCode());
        expectStatus(target, AdapterScenarioPaths.ERROR_NOT_FOUND, 404, NexusStatusCode.RESOURCE_NOT_FOUND.fullCode());
        expectStatus(target, AdapterScenarioPaths.ERROR_RATE_LIMIT, 429, NexusStatusCode.LIMIT_EXCEEDED.fullCode());
        expectStatus(target, AdapterScenarioPaths.ERROR_INTERNAL, 500, NexusStatusCode.SYSTEM_ERROR.fullCode());
    }

    private void expectStatus(AdapterTestTarget target, String path, int status, String code) {
        HttpTestClient.HttpExchange exchange = client.get(target, path);
        require(exchange.status() == status, "unexpected status for " + path + ": " + exchange.status());
        require(exchange.firstHeader(StandardHeaders.REQUEST_ID).isPresent(), "missing request id for " + path);
        if (exchange.body().isBlank()) {
            return;
        }
        Map<String, Object> body = Jsons.toMap(exchange.body());
        Object bodyCode = body.get("code");
        if (bodyCode != null) {
            require(code.equals(bodyCode.toString()), "unexpected code for " + path + ": " + bodyCode);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
