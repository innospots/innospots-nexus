package com.innospots.nexus.ai.runtime;

import java.time.Instant;

/**
 * 不可变运行状态快照。
 */
public record AiRuntimeHealth(
        AiRuntimeStatus state,
        Instant since,
        String runtimeVersion,
        String nodeVersion,
        long pid,
        Instant lastCheckAt,
        long latencyMs,
        String configVersion,
        String error) {

    /**
     * 是否可接业务流量。
     *
     * @return true 表示 RUNNING 且无错误
     */
    public boolean ready() {
        return state == AiRuntimeStatus.RUNNING && error == null;
    }
}
