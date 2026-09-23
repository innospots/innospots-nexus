package com.innospots.nexus.service.contract.cancellation;

/**
 * 调用或流被取消的原因。
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
