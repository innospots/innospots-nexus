package com.innospots.nexus.ai;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import com.innospots.nexus.ai.runtime.AiEndpoint;
import com.innospots.nexus.ai.runtime.AiRuntime;
import com.innospots.nexus.ai.runtime.AiRuntimeHealth;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;

/**
 * 测试用 runtime。
 */
public class FakeAiRuntime implements AiRuntime {

    private AiRuntimeStatus state = AiRuntimeStatus.STOPPED;
    private AiEndpoint endpoint;
    private String error;

    public FakeAiRuntime(AiEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void state(AiRuntimeStatus state) {
        this.state = state;
    }

    public void error(String error) {
        this.error = error;
    }

    @Override
    public void start() {
        state = AiRuntimeStatus.RUNNING;
    }

    @Override
    public void stop() {
        state = AiRuntimeStatus.STOPPED;
    }

    @Override
    public AiRuntimeStatus status() {
        return state;
    }

    @Override
    public AiRuntimeHealth health() {
        return new AiRuntimeHealth(state, Instant.now(), "0.1.0", "v22.19.0", 1,
                Instant.now(), 1, "sha256:test", error);
    }

    @Override
    public Optional<AiEndpoint> endpoint() {
        return Optional.ofNullable(endpoint);
    }
}
