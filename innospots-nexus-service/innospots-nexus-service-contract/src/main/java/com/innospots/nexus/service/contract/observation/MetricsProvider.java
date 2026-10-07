package com.innospots.nexus.service.contract.observation;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * 启动调用观测。标签基数由实现方管理。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationObservation
 */
public interface MetricsProvider {

    /**
     * 为 {@code invocation} 开始观测。
     *
     * @param invocation 当前调用
     * @return 观测句柄
     */
    InvocationObservation begin(InvocationContext invocation);
}
