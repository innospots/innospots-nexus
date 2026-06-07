package com.innospots.nexus.base.execution;

/**
 * Lifecycle states for an execution from creation through to completion.
 */
public enum ExecutionStatus {
    CREATED,
    STARTING,
    READY,
    PENDING,
    RUNNING,
    STOPPING,
    STOPPED,
    SUCCESS,
    FAILED
}
