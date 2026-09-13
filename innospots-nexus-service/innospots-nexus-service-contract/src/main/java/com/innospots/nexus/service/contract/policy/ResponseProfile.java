package com.innospots.nexus.service.contract.policy;

/**
 * HTTP error body profile. Default is {@link #LEGACY} {@code R<T>}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 */
public enum ResponseProfile {
    LEGACY,
    PROBLEM
}
