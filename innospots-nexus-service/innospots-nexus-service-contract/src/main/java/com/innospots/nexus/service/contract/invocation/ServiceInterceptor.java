package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * 围绕服务调用的有序拦截器。进入失败则跳过后续拦截器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationLease
 * @see InterceptorOrders
 * @see InterceptorIds
 */
public interface ServiceInterceptor {

    /**
     * 返回拦截器标识。
     *
     * @return 稳定标识
     */
    String id();

    /**
     * 返回拦截器顺序。较小值在进入时先执行。
     *
     * @return 顺序值
     */
    int order();

    /**
     * 进入拦截器并返回稍后完成的租约。
     *
     * @param invocation 当前调用
     * @return 租约
     */
    CompletionStage<InvocationLease> enter(InvocationContext invocation);
}
