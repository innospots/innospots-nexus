package com.innospots.nexus.service.contract.invocation;

/**
 * 调用的终态分类。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationOutcome
 */
public enum OutcomeType {
    SUCCEEDED,
    FAILED,
    CANCELLED,
    TIMED_OUT,
    REJECTED
}
