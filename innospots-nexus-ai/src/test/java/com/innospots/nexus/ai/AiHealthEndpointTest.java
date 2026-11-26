package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.ai.health.AiHealthEndpoint;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;

class AiHealthEndpointTest {

    @Test
    void liveIsAlwaysUp() {
        FakeAiRuntime runtime = new FakeAiRuntime(null);
        AiHealthEndpoint endpoint = new AiHealthEndpoint(runtime, "0.1.0");
        assertThat(endpoint.live().getStatus()).isEqualTo(200);
    }

    @Test
    void readyReflectsRuntimeState() {
        FakeAiRuntime runtime = new FakeAiRuntime(null);
        AiHealthEndpoint endpoint = new AiHealthEndpoint(runtime, "0.1.0");
        assertThat(endpoint.ready().getStatus()).isEqualTo(503);
        runtime.state(AiRuntimeStatus.RUNNING);
        assertThat(endpoint.ready().getStatus()).isEqualTo(200);
        runtime.error("config invalid");
        assertThat(endpoint.ready().getStatus()).isEqualTo(503);
    }

    @Test
    void aggregateHealthReturns503WhenStopped() {
        FakeAiRuntime runtime = new FakeAiRuntime(null);
        AiHealthEndpoint endpoint = new AiHealthEndpoint(runtime, "0.1.0");
        assertThat(endpoint.health().getStatus()).isEqualTo(503);
        runtime.state(AiRuntimeStatus.RUNNING);
        assertThat(endpoint.health().getStatus()).isEqualTo(200);
    }
}
