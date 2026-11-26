package com.innospots.nexus.ai.runtime;

/**
 * 启动后组合 supervisor 的默认实现。
 */
public final class DefaultAiRuntime implements AiRuntime {

    private final AiProcessSupervisor supervisor;

    public DefaultAiRuntime(com.innospots.nexus.ai.config.AiRuntimeConfig config) {
        this.supervisor = new AiProcessSupervisor(config);
    }

    public DefaultAiRuntime(AiProcessSupervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public void start() {
        supervisor.start();
    }

    @Override
    public void stop() {
        supervisor.stop();
    }

    @Override
    public AiRuntimeStatus status() {
        return supervisor.status();
    }

    @Override
    public AiRuntimeHealth health() {
        return supervisor.health();
    }

    @Override
    public java.util.Optional<AiEndpoint> endpoint() {
        return supervisor.endpoint();
    }
}
