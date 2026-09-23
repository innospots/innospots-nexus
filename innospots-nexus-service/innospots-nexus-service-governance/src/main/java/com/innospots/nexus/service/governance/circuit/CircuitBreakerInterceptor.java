package com.innospots.nexus.service.governance.circuit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.CircuitBreakerProvider;
import com.innospots.nexus.service.contract.governance.CircuitPermit;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;

/**
 * 将 {@link com.innospots.nexus.service.contract.policy.OperationPolicy#circuitKey()} 转为
 * {@link CircuitBreakerProvider#acquire(String)} 与终态 {@link CircuitPermit#finish} 的拦截器。
 *
 * <p><strong>行为</strong>：enter 阶段同步 {@code acquire}（OPEN 时异常失败）；返回的 lease 在
 * {@link InvocationLease#finish} 时将 {@link com.innospots.nexus.service.contract.invocation.InvocationOutcome}
 * 交给 Provider 更新滑动窗口。</p>
 *
 * <p><strong>使用场景</strong>：包裹下游 Client/外部 API 调用，不宜作为全站入站 HTTP 默认过滤器。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see CircuitBreakerProvider
 * @see InterceptorIds#CIRCUIT
 */
public final class CircuitBreakerInterceptor implements ServiceInterceptor {

    /** 断路器 SPI；enter 时 acquire，finish 时上报 {@link com.innospots.nexus.service.contract.invocation.InvocationOutcome}。 */
    private final CircuitBreakerProvider provider;

    /**
     * 创建拦截器。
     *
     * @param provider 断路 SPI，通常为 {@link Resilience4jCircuitBreakerProvider}
     */
    public CircuitBreakerInterceptor(CircuitBreakerProvider provider) {
        this.provider = Checks.notNull(provider, "provider");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorIds#CIRCUIT}
     */
    @Override
    public String id() {
        return InterceptorIds.CIRCUIT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorOrders#CIRCUIT}
     */
    @Override
    public int order() {
        return InterceptorOrders.CIRCUIT;
    }

    /**
     * 获取断路许可并注册终态上报 lease。
     *
     * @param invocation 当前调用
     * @return 在 finish 时调用 {@link CircuitPermit#finish} 的 lease
     */
    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        String policyKey = invocation.policy().circuitKey();
        if (policyKey == null) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        CircuitPermit permit = provider.acquire(policyKey);
        return CompletableFuture.completedFuture(outcome -> {
            permit.finish(outcome);
            return CompletableFuture.completedFuture(null);
        });
    }
}
