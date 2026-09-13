package com.innospots.nexus.service.contract.cancellation;

/**
 * Why an invocation or stream was cancelled.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationToken
 */
public enum CancellationReason {
    CLIENT_DISCONNECTED,
    DEADLINE_EXCEEDED,
    APPLICATION_CANCELLED,
    SERVER_SHUTDOWN
}
