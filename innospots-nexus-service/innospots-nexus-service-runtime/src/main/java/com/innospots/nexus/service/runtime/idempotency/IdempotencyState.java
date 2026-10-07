package com.innospots.nexus.service.runtime.idempotency;

/**
 * 幂等项状态。
 */
public enum IdempotencyState {

    IN_FLIGHT,
    SUCCEEDED,
    FAILED,
    UNKNOWN
}
