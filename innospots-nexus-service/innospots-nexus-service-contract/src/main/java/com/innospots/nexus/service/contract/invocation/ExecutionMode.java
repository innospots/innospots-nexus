package com.innospots.nexus.service.contract.invocation;

/**
 * 业务供应方如何执行。
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
