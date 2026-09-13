package com.innospots.nexus.service.runtime.time;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * Rejects an already expired deadline before business execution.
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.time.Deadline
 * @see ServiceStatusCode#DEADLINE_EXCEEDED
 */
public final class DeadlineInterceptor implements ServiceInterceptor {

    @Override
    public String id() {
        return InterceptorIds.DEADLINE;
    }

    @Override
    public int order() {
        return InterceptorOrders.DEADLINE;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        if (invocation.service().deadline().isExpired()) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED));
        }
        return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
    }
}
