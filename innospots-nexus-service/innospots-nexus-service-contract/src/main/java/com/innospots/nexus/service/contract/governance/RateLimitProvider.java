package com.innospots.nexus.service.contract.governance;

/**
 * 限流<strong>提供方 SPI</strong>：对单次调用尝试扣减配额并返回是否允许继续。
 *
 * <p><strong>用途</strong>：将「是否允许进入业务」与具体算法（本地令牌桶、Redis 等）解耦。
 * {@link com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor} 在
 * {@code InvocationEngine} 进入阶段调用本接口；拒绝时映射 HTTP 429 /
 * {@link com.innospots.nexus.base.status.NexusStatusCode#LIMIT_EXCEEDED}。</p>
 *
 * <p><strong>使用场景</strong>：入站 API 按操作/主体/租户组合限流；保护下游前的粗粒度配额。
 * 不适用于需要排队等待的场景（契约要求立即返回决策）。</p>
 *
 * <p><strong>实现约束</strong>：</p>
 * <ul>
 *   <li>{@link #acquire(RateLimitRequest)} 不得阻塞线程等待令牌补充。</li>
 *   <li>对未知 {@code policyKey} 的行为由实现定义；默认实现抛出配置错误。</li>
 *   <li>应线程安全；高并发下宜避免在持锁期间执行用户回调。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitRequest
 * @see RateLimitDecision
 * @see com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider
 */
public interface RateLimitProvider {

    /**
     * 尝试为本次调用获取限流配额。
     *
     * <p>调用方（拦截器）在 {@code allowed == false} 时必须拒绝进入业务，并可依据
     * {@link RateLimitDecision#retryAfter()} 向客户端建议重试间隔（HTTP {@code Retry-After}）。</p>
     *
     * @param request 策略键、维度与单次成本；{@code request} 不可为 {@code null}
     * @return 非 {@code null} 的决策；{@code retryAfter} 永不为负
     */
    RateLimitDecision acquire(RateLimitRequest request);
}
