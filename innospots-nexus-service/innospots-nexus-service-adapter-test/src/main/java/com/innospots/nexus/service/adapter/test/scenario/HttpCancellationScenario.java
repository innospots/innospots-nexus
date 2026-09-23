package com.innospots.nexus.service.adapter.test.scenario;

import java.time.Duration;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 客户端断开或超时后服务端取消处理。
 */
public final class HttpCancellationScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        try {
            client.get(target, AdapterScenarioPaths.SLOW + "?delayMs=5000", Duration.ofMillis(200));
            throw new AssertionError("expected timeout");
        } catch (IllegalStateException ex) {
            require(ex.getCause() instanceof java.net.http.HttpTimeoutException, "expected timeout cause");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
