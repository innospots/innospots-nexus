package com.innospots.nexus.ai.runtime;

/**
 * AI runtime 生命周期状态。
 */
public enum AiRuntimeStatus {
    STOPPED,
    STARTING,
    RUNNING,
    DEGRADED,
    STOPPING,
    FAILED
}
