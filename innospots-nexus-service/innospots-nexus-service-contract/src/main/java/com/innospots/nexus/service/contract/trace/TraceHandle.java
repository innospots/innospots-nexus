package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * 由 {@link TraceProvider} 创建的活跃 Span 句柄。
 *
 * @author Smars
 * @date 2026/09/13
 * @see TraceProvider
 */
public interface TraceHandle {

    /**
     * 返回活跃 Span 的快照。
     *
     * @return 当前快照
     */
    TraceSnapshot snapshot();

    /**
     * 以调用结果完成 Span。
     *
     * @param outcome 终态结果
     */
    void finish(InvocationOutcome outcome);
}
