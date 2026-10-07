package com.innospots.nexus.spring.service.test;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.innospots.nexus.service.adapter.test.runner.AdapterScenarioRunner;
import com.innospots.nexus.service.adapter.test.scenario.StreamSseScenario;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 在嵌入式 MVC 宿主上运行共享 adapter 场景。
 */
@SpringBootTest(classes = MvcTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdapterScenarioMvcTest {

    @LocalServerPort
    private int port;

    @Test
    void runsSharedAdapterScenarios() {
        AdapterTestTarget target = new AdapterTestTarget(URI.create("http://127.0.0.1:" + port), Map.of());
        new AdapterScenarioRunner().runAll(target);
    }

    @Test
    void streamsStreamSessionAsSse() {
        AdapterTestTarget target = new AdapterTestTarget(URI.create("http://127.0.0.1:" + port), Map.of());
        new StreamSseScenario().run(target);
    }
}
