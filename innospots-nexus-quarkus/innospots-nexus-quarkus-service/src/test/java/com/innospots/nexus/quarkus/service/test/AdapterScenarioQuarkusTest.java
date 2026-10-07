package com.innospots.nexus.quarkus.service.test;

import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.innospots.nexus.service.adapter.test.runner.AdapterScenarioRunner;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * 在嵌入式 Quarkus 宿主上运行共享 adapter 场景。
 */
@QuarkusTest
class AdapterScenarioQuarkusTest {

    @TestHTTPResource
    URL url;

    @Test
    void runsSharedAdapterScenarios() {
        AdapterTestTarget target = new AdapterTestTarget(URI.create(url.toString()), Map.of());
        new AdapterScenarioRunner().runAll(target);
    }
}
