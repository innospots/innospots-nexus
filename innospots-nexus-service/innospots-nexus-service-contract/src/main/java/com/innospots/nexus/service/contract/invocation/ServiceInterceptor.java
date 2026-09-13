package com.innospots.nexus.service.contract.invocation;

import java.util.concurrent.CompletionStage;

/**
 * Ordered interceptor around a service invocation. Failed enter skips later interceptors.
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationLease
 * @see InterceptorOrders
 * @see InterceptorIds
 */
public interface ServiceInterceptor {

    /**
     * Returns the interceptor identifier.
     *
     * @return stable id
     */
    String id();

    /**
     * Returns the interceptor order. Lower values run first on enter.
     *
     * @return order
     */
    int order();

    /**
     * Enters the interceptor and returns a lease to finish later.
     *
     * @param invocation current invocation
     * @return lease
     */
    CompletionStage<InvocationLease> enter(InvocationContext invocation);
}
