package com.innospots.nexus.ai.health;

import java.time.Duration;

import com.innospots.nexus.ai.config.AiRuntimeConfig;
import com.innospots.nexus.ai.runtime.AiRuntime;
import com.innospots.nexus.ai.runtime.AiRuntimeHealth;
import com.innospots.nexus.ai.runtime.AiRuntimeStatus;

/**
 * 业务请求前的可用性门禁。
 */
public final class AiAvailabilityGuard {

    private final AiRuntime runtime;
    private final AiRuntimeConfig config;

    public AiAvailabilityGuard(AiRuntime runtime, AiRuntimeConfig config) {
        this.runtime = runtime;
        this.config = config;
    }

    /**
     * 确保 runtime 可以接收请求。
     */
    public synchronized void ensure() {
        AiRuntimeHealth health = runtime.health();
        if (health.ready()) {
            return;
        }
        if (health.state() == AiRuntimeStatus.STOPPED
                && config.startupPolicy() == AiRuntimeConfig.StartupPolicy.LAZY) {
            try {
                runtime.start();
                health = runtime.health();
                if (health.ready()) {
                    return;
                }
            } catch (RuntimeException e) {
                throw new AiUnavailableException("runtime_unavailable",
                        e.getMessage(), AiRuntimeStatus.FAILED, Duration.ofSeconds(10));
            }
        }
        switch (health.state()) {
            case STARTING:
                throw new AiUnavailableException("runtime_starting", "AI runtime is starting",
                        health.state(), Duration.ofSeconds(2));
            case DEGRADED:
                throw new AiUnavailableException("runtime_degraded", "AI runtime is degraded",
                        health.state(), Duration.ofSeconds(5));
            case FAILED:
                throw new AiUnavailableException("runtime_unavailable", "AI runtime failed",
                        health.state(), Duration.ofSeconds(10));
            case STOPPED:
                throw new AiUnavailableException("runtime_unavailable", "AI runtime is not started",
                        health.state(), Duration.ofSeconds(2));
            default:
                throw new AiUnavailableException("runtime_unavailable", "AI runtime is not ready",
                        health.state(), Duration.ofSeconds(5));
        }
    }
}
