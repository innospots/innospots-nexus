package com.innospots.nexus.service.contract.invocation;

/**
 * How the business supplier may execute.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.policy.annotation.Execution
 */
public enum ExecutionMode {
    NON_BLOCKING,
    BLOCKING,
    CPU_INTENSIVE
}
