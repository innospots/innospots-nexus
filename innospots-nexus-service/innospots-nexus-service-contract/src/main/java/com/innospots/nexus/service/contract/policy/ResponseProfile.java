package com.innospots.nexus.service.contract.policy;

/**
 * HTTP 错误体配置。默认为 {@link #LEGACY} {@code R<T>}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see OperationPolicy
 */
public enum ResponseProfile {
    LEGACY,
    PROBLEM
}
