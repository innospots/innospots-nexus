package com.innospots.nexus.service.contract.governance;

import java.util.List;

import com.innospots.nexus.base.util.Checks;

/**
 * 单次限流判定输入。
 *
 * <p><strong>用途</strong>：把「用哪条策略」与「对谁限流」交给 {@link RateLimitProvider}，
 * 由实现按 {@code policyKey} 查配置并对 {@code dimensions} 做组合扣减（默认实现为 AND 语义）。</p>
 *
 * <p><strong>典型构造</strong>（由 {@code RateLimitInterceptor} 生成）：</p>
 * <ul>
 *   <li>{@code policyKey} — 来自 {@link com.innospots.nexus.service.contract.policy.OperationPolicy#rateLimitKey()}，
 *       对应 {@code GovernanceConfig.rateLimits} 表键。</li>
 *   <li>{@code dimensions} — 如 {@code operation:…}、{@code principal:…}、{@code realm:…}、
 *       可选 {@code tenant:…}；空列表表示仅按 {@code policyKey} 单桶限流。</li>
 *   <li>{@code cost} — 本次消耗令牌数，拦截器默认为 {@code 1}。</li>
 * </ul>
 *
 * <p><strong>约束</strong>：{@code cost} 必须为正；{@code dimensions} 在 compact 构造中复制为不可变列表。</p>
 *
 * @param policyKey  治理配置中的限流策略键，非空
 * @param dimensions 组合限流维度标签，可为空列表
 * @param cost       本次请求消耗的令牌数，必须 &gt; 0
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitProvider
 * @see RateLimitDecision
 */
public record RateLimitRequest(
        /** 治理配置 {@code rateLimits} 中的策略键，与 {@link com.innospots.nexus.service.contract.policy.annotation.RateLimited} 一致。 */
        String policyKey,
        /** 组合限流维度（如 operation/principal/tenant）；空列表表示仅按 {@code policyKey} 单桶。 */
        List<String> dimensions,
        /** 本次请求消耗的令牌数，拦截器路径下通常为 {@code 1}。 */
        int cost) {

    /**
     * 校验并规范化请求字段。
     *
     * @param policyKey  策略键
     * @param dimensions 维度列表，{@code null} 视为空
     * @param cost       令牌成本
     */
    public RateLimitRequest {
        Checks.notBlank(policyKey, "policyKey");
        dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
        Checks.positive(cost, "cost");
    }
}
