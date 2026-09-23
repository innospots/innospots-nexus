package com.innospots.nexus.service.runtime.audit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 审计意图检查与终态事件分发拦截器。
 */
public final class AuditInterceptor implements ServiceInterceptor {

    private final AuditDispatcher dispatcher;
    private final boolean auditEnabled;

    /**
     * 创建拦截器。
     *
     * @param dispatcher   审计分发器
     * @param auditEnabled 是否启用审计
     */
    public AuditInterceptor(AuditDispatcher dispatcher, boolean auditEnabled) {
        this.dispatcher = Checks.notNull(dispatcher, "dispatcher");
        this.auditEnabled = auditEnabled;
    }

    @Override
    public String id() {
        return InterceptorIds.AUDIT;
    }

    @Override
    public int order() {
        return InterceptorOrders.AUDIT;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        if (!auditEnabled || !invocation.policy().audited()) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        if (invocation.policy().auditMode() == AuditMode.REQUIRED && !dispatcher.supportsRequired()) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.AUDIT_UNAVAILABLE));
        }
        return CompletableFuture.completedFuture(outcome ->
                dispatcher.dispatch(
                        invocation.policy().auditMode(),
                        AuditEvents.from(invocation, outcome)));
    }
}
