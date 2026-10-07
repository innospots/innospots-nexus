package com.innospots.nexus.service.governance.bulkhead;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.BulkheadPermit;
import com.innospots.nexus.service.contract.governance.BulkheadProvider;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 将 {@link com.innospots.nexus.service.contract.policy.OperationPolicy#bulkheadKey()} 转为
 * {@link BulkheadProvider#tryAcquire(String)} 的 {@link ServiceInterceptor}。
 *
 * <p><strong>行为</strong>：无策略键则透传；{@code tryAcquire} 失败时
 * {@link ServiceStatusCode#CAPACITY_EXHAUSTED}。成功时返回 lease，在
 * {@link InvocationLease#finish} 阶段关闭 {@link BulkheadPermit} 释放并发槽位。</p>
 *
 * <p><strong>约束</strong>：默认 Provider 不排队；业务不协作终止时许可可能延迟释放（workTermination 模型）。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see BulkheadProvider
 * @see InterceptorIds#BULKHEAD
 */
public final class BulkheadInterceptor implements ServiceInterceptor {

    /** 舱壁许可 SPI；enter 时 tryAcquire，finish 时 close permit。 */
    private final BulkheadProvider provider;

    /**
     * 创建拦截器。
     *
     * @param provider 舱壁 SPI，通常为 {@link SemaphoreBulkheadProvider}
     */
    public BulkheadInterceptor(BulkheadProvider provider) {
        this.provider = Checks.notNull(provider, "provider");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorIds#BULKHEAD}
     */
    @Override
    public String id() {
        return InterceptorIds.BULKHEAD;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorOrders#BULKHEAD}
     */
    @Override
    public int order() {
        return InterceptorOrders.BULKHEAD;
    }

    /**
     * 尝试获取舱壁许可。
     *
     * @param invocation 当前调用
     * @return 含释放逻辑的 lease，或 failedFuture
     */
    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        String policyKey = invocation.policy().bulkheadKey();
        if (policyKey == null) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        Optional<BulkheadPermit> permit = provider.tryAcquire(policyKey);
        if (permit.isEmpty()) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.CAPACITY_EXHAUSTED));
        }
        BulkheadPermit acquired = permit.get();
        return CompletableFuture.completedFuture(outcome -> {
            acquired.close();
            return CompletableFuture.completedFuture(null);
        });
    }
}
