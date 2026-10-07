package com.innospots.nexus.service.governance.config;

import java.time.Duration;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * 治理运行时<strong>总配置</strong>：策略表、限流桶容量与传输默认超时时长。
 *
 * <p><strong>用途</strong>：集中承载注解策略键到具体数值的映射，供
 * {@link com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider}、
 * {@link com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider}、
 * {@link com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider}、
 * {@link com.innospots.nexus.service.governance.timeout.TimeoutInterceptor} 使用。</p>
 *
 * <p><strong>使用方式</strong>：在 adapter 模块定义 {@code GovernanceConfig} Bean，例如：</p>
 * <pre>{@code
 * new GovernanceConfig(
 *     Map.of("promo.claim", new RateLimitPolicy(10, 1.0)),
 *     Map.of("export", new BulkheadPolicy(4)),
 *     Map.of("payment.client", CircuitBreakerPolicy.defaults()),
 *     Map.of("op.slow", Duration.ofSeconds(5)),
 *     100_000,
 *     Duration.ofMinutes(15),
 *     Duration.ofSeconds(30),
 *     Duration.ofMinutes(60),
 *     Duration.ofSeconds(30));
 * }</pre>
 *
 * <p><strong>约束</strong>：各 Map 在 compact 构造中不可变复制；{@code maxRateLimitKeys} 必须为正；
 * 默认 HTTP/流/WS 超时常用于 {@code DeadlineInterceptor} 上限，与 {@code timeouts} 表中的操作级超时不同。</p>
 *
 * @param rateLimits              限流策略键 → {@link RateLimitPolicy}
 * @param bulkheads               舱壁策略键 → {@link BulkheadPolicy}
 * @param circuits                断路策略键 → {@link CircuitBreakerPolicy}
 * @param timeouts                操作或 {@link com.innospots.nexus.service.contract.policy.OperationPolicy#timeoutPolicyKey()} → 超时时长
 * @param maxRateLimitKeys        单 JVM 限流桶 Map 上限（仅 {@link com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider}）
 * @param rateLimitIdleTtl        闲置桶淘汰 TTL（仅本地 Provider，配合上限触发淘汰）
 * @param defaultHttpTimeout      普通 HTTP 请求 deadline 默认上限
 * @param defaultStreamTimeout    流式会话默认上限
 * @param defaultWebSocketTimeout WebSocket 单条消息处理默认上限
 * @author Smars
 * @date 2026/09/15
 * @see RateLimitPolicy
 * @see BulkheadPolicy
 * @see CircuitBreakerPolicy
 */
public record GovernanceConfig(
        /** 限流注解键 → {@link RateLimitPolicy}。 */
        Map<String, RateLimitPolicy> rateLimits,
        /** 舱壁注解键 → {@link BulkheadPolicy}。 */
        Map<String, BulkheadPolicy> bulkheads,
        /** 断路注解键 → {@link CircuitBreakerPolicy}。 */
        Map<String, CircuitBreakerPolicy> circuits,
        /** 超时策略键或 operationId → 操作级 {@link Duration}。 */
        Map<String, Duration> timeouts,
        /** 单 JVM 限流桶 Map 最大条目数，防止键爆炸。 */
        int maxRateLimitKeys,
        /** 超过容量时淘汰 idle 超过此时长的桶。 */
        Duration rateLimitIdleTtl,
        /** HTTP 请求全局 deadline 默认上限（adapter/deadline 层）。 */
        Duration defaultHttpTimeout,
        /** 流式会话全局 deadline 默认上限。 */
        Duration defaultStreamTimeout,
        /** WebSocket 单条消息处理默认上限。 */
        Duration defaultWebSocketTimeout
) {

    /**
     * 校验并规范化所有字段。
     */
    public GovernanceConfig {
        rateLimits = rateLimits == null ? Map.of() : Map.copyOf(rateLimits);
        bulkheads = bulkheads == null ? Map.of() : Map.copyOf(bulkheads);
        circuits = circuits == null ? Map.of() : Map.copyOf(circuits);
        timeouts = timeouts == null ? Map.of() : Map.copyOf(timeouts);
        Checks.isTrue(maxRateLimitKeys > 0, "maxRateLimitKeys must be positive");
        Checks.notNull(rateLimitIdleTtl, "rateLimitIdleTtl");
        Checks.isTrue(!rateLimitIdleTtl.isNegative(), "rateLimitIdleTtl must not be negative");
        Checks.notNull(defaultHttpTimeout, "defaultHttpTimeout");
        Checks.notNull(defaultStreamTimeout, "defaultStreamTimeout");
        Checks.notNull(defaultWebSocketTimeout, "defaultWebSocketTimeout");
    }

    /**
     * 返回空策略表与文档化默认容量的配置，供测试或最小装配使用。
     *
     * @return 无业务策略、限流桶上限 100_000、idle TTL 15 分钟的配置
     */
    public static GovernanceConfig defaults() {
        return new GovernanceConfig(
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                100_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
    }
}
