package com.innospots.nexus.service.runtime.lifecycle;

/**
 * {@link ServiceRuntime} 生命周期状态。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum RuntimeState {
    CREATED,
    STARTING,
    READY,
    STOPPING,
    STOPPED,
    CLOSED,
    FAILED
}
