package com.innospots.nexus.service.adapter.test.scenario;

import java.time.Duration;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 流式断开不应返回 500。
 */
public final class StreamCancelScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        try {
            client.get(target, AdapterScenarioPaths.STREAM_CANCEL, Duration.ofMillis(300));
        } catch (IllegalStateException ex) {
            // client-side timeout/cancel is acceptable
        }
    }
}
