package com.innospots.nexus.spring.service.test;

import java.net.URI;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.innospots.nexus.service.adapter.test.runner.AdapterScenarioRunner;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 在嵌入式 WebFlux 宿主上运行共享 adapter 场景。
 */
@SpringBootTest(classes = WebFluxTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webflux")
@TestPropertySource(properties = "spring.main.web-application-type=reactive")
class AdapterScenarioWebFluxTest {

    @LocalServerPort
    private int port;

    @Test
    void runsSharedAdapterScenarios() {
        AdapterTestTarget target = new AdapterTestTarget(URI.create("http://127.0.0.1:" + port), Map.of());
        new AdapterScenarioRunner().runAll(target);
    }
}
