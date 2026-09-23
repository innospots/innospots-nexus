package com.innospots.nexus.service.runtime.invocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;

/**
 * 默认链中的首个拦截器。逻辑结果由 {@link InvocationEngine} 持有。
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationEngine
 */
public final class ControlInterceptor implements ServiceInterceptor {

    @Override
    public String id() {
        return InterceptorIds.CONTROL;
    }

    @Override
    public int order() {
        return InterceptorOrders.CONTROL;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
    }
}
