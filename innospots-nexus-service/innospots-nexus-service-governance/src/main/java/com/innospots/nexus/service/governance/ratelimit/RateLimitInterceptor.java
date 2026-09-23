package com.innospots.nexus.service.governance.ratelimit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.RateLimitDecision;
import com.innospots.nexus.service.contract.governance.RateLimitProvider;
import com.innospots.nexus.service.contract.governance.RateLimitRequest;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;

/**
 * 将 {@link com.innospots.nexus.service.contract.policy.OperationPolicy#rateLimitKey()} 转为
 * {@link RateLimitProvider} 调用的 {@link ServiceInterceptor}。
 *
 * <p><strong>行为</strong>：若策略键为 {@code null} 则透传；否则构造
 * {@link RateLimitRequest}（含 {@link RateLimitDimensions} 解析的维度标签，cost=1），
 * 调用 {@link RateLimitProvider#acquire(RateLimitRequest)}。拒绝时以
 * {@link NexusStatusCode#LIMIT_EXCEEDED} 失败进入阶段，Outcome 为 REJECTED。</p>
 *
 * <p><strong>约束</strong>：本拦截器不持有需释放的 lease（许可在 Provider 内即时扣减）；
 * 当前实现未将 {@link RateLimitDecision#retryAfter()} 写入异常，HTTP Retry-After 由 adapter 策略补充。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see RateLimitProvider
 * @see InterceptorIds#RATE_LIMIT
 */
public final class RateLimitInterceptor implements ServiceInterceptor {

    /** 限流判定 SPI；由装配层注入，可替换为 Redis 等集群实现。 */
    private final RateLimitProvider provider;

    /** 策略表，用于解析维度模式与客户维度。 */
    private final GovernanceConfig config;

    /**
     * 创建拦截器。
     *
     * @param provider 限流 SPI 实现
     * @param config   治理配置（含 {@code rateLimits} 策略表）
     */
    public RateLimitInterceptor(RateLimitProvider provider, GovernanceConfig config) {
        this.provider = Checks.notNull(provider, "provider");
        this.config = Checks.notNull(config, "config");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorIds#RATE_LIMIT}
     */
    @Override
    public String id() {
        return InterceptorIds.RATE_LIMIT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link InterceptorOrders#RATE_LIMIT}
     */
    @Override
    public int order() {
        return InterceptorOrders.RATE_LIMIT;
    }

    /**
     * 在进入业务前执行限流判定。
     *
     * @param invocation 当前调用上下文，含已解析 {@link com.innospots.nexus.service.contract.policy.OperationPolicy}
     * @return 成功时为无操作 lease；拒绝时为 failedFuture
     */
    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        String policyKey = invocation.policy().rateLimitKey();
        if (policyKey == null) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        RateLimitPolicy policy = config.rateLimits().get(policyKey);
        if (policy == null) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.CONFIG_ERROR));
        }
        RateLimitRequest request = new RateLimitRequest(
                policyKey,
                RateLimitDimensions.resolve(invocation, policy),
                1);
        RateLimitDecision decision = provider.acquire(request);
        if (!decision.allowed()) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.LIMIT_EXCEEDED));
        }
        return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
    }
}
