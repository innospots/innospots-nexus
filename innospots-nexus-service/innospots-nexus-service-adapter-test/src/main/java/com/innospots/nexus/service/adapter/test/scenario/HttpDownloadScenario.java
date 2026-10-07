package com.innospots.nexus.service.adapter.test.scenario;

import com.innospots.nexus.service.adapter.test.client.HttpTestClient;
import com.innospots.nexus.service.adapter.test.fixture.AdapterDownloadFixtures;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 内存下载写回烟雾测试。
 */
public final class HttpDownloadScenario implements AdapterScenario {

    private final HttpTestClient client = new HttpTestClient();

    @Override
    public void run(AdapterTestTarget target) {
        HttpTestClient.HttpExchange exchange = client.get(target, AdapterScenarioPaths.DOWNLOAD);
        require(exchange.status() == 200, "expected 200");
        require(exchange.body().equals(new String(AdapterDownloadFixtures.DOWNLOAD_BYTES)), "download body mismatch");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
