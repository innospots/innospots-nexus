package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * 为调用启动子 Span。适配器调用此方法；业务代码使用 {@code @Traced}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see TraceHandle
 * @see TraceSnapshot
 */
public interface TraceProvider {

    /**
     * 在 {@code parent} 下为 {@code invocation} 启动 Span。
     *
     * @param invocation 当前调用
     * @param parent     父快照，可能为空
     * @return 必须完成的句柄
     */
    TraceHandle start(InvocationContext invocation, TraceSnapshot parent);
}
