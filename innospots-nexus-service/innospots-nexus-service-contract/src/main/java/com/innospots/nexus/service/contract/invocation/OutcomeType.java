package com.innospots.nexus.service.contract.invocation;

/**
 * Terminal classification of an invocation.
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
