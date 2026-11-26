package com.innospots.nexus.ai.runtime;

import java.util.Optional;

/**
 * AI runtime 宿主公共契约。
 */
public interface AiRuntime extends AutoCloseable {

    void start();

    void stop();

    AiRuntimeStatus status();

    AiRuntimeHealth health();

    Optional<AiEndpoint> endpoint();

    @Override
    default void close() {
        stop();
    }
}
