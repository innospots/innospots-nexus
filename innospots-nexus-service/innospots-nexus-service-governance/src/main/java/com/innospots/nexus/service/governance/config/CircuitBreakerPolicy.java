package com.innospots.nexus.service.governance.config;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;

/**
 * 单键<strong>断路器</strong>参数，与 Resilience4j {@link io.github.resilience4j.circuitbreaker.CircuitBreakerConfig} 对齐。
 *
 * <p><strong>用途</strong>：由
 * {@link com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider}
 * 在首次 {@code acquire} 时懒创建 {@link io.github.resilience4j.circuitbreaker.CircuitBreaker}。
 * 状态机 CLOSED → OPEN → HALF_OPEN 由库驱动，探测由后续调用触发。</p>
 *
 * <p><strong>建议</strong>：生产环境使用 {@link #defaults()} 作为起点，再按下游 SLO 调参；
 * {@code policyKey} 应对应稳定下游名，而非 HTTP 路径模板。</p>
 *
 * @param slidingWindowSize                  滑动窗口采样次数
 * @param minimumNumberOfCalls               达到后才计算失败/慢调用率
 * @param failureRateThreshold               失败率阈值，0–100
 * @param slowCallRateThreshold              慢调用率阈值，0–100
 * @param slowCallDurationThreshold          超过此时长计为慢调用
 * @param waitDurationInOpenState            OPEN 状态最短持续时间
 * @param permittedNumberOfCallsInHalfOpenState HALF_OPEN 允许试探次数
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.config.GovernanceConfig#circuits()
 */
public record CircuitBreakerPolicy(
        /** Resilience4j 滑动窗口采样次数。 */
        int slidingWindowSize,
        /** 窗口内至少多少次调用后才计算比率。 */
        int minimumNumberOfCalls,
        /** 失败率超过该百分比（0–100）可触发 OPEN。 */
        float failureRateThreshold,
        /** 慢调用率超过该百分比（0–100）可触发 OPEN。 */
        float slowCallRateThreshold,
        /** 超过该耗时的成功调用计为慢调用。 */
        Duration slowCallDurationThreshold,
        /** OPEN 状态最短保持时间，之后可进入 HALF_OPEN。 */
        Duration waitDurationInOpenState,
        /** HALF_OPEN 状态下允许通过的试探调用次数。 */
        int permittedNumberOfCallsInHalfOpenState
) {

    /**
     * 校验窗口、阈值与时长字段合法。
     */
    public CircuitBreakerPolicy {
        Checks.isTrue(slidingWindowSize > 0, "slidingWindowSize must be positive");
        Checks.isTrue(minimumNumberOfCalls > 0, "minimumNumberOfCalls must be positive");
        Checks.isTrue(failureRateThreshold >= 0F && failureRateThreshold <= 100F, "failureRateThreshold out of range");
        Checks.isTrue(slowCallRateThreshold >= 0F && slowCallRateThreshold <= 100F, "slowCallRateThreshold out of range");
        Checks.notNull(slowCallDurationThreshold, "slowCallDurationThreshold");
        Checks.notNull(waitDurationInOpenState, "waitDurationInOpenState");
        Checks.isTrue(!slowCallDurationThreshold.isNegative(), "slowCallDurationThreshold must not be negative");
        Checks.isTrue(!waitDurationInOpenState.isNegative(), "waitDurationInOpenState must not be negative");
        Checks.isTrue(permittedNumberOfCallsInHalfOpenState > 0, "permittedNumberOfCallsInHalfOpenState must be positive");
    }

    /**
     * 返回与设计文档一致的建议默认策略（window=100、minimumCalls=20、失败/慢调用率 50% 等）。
     *
     * @return 可用于新下游接入的默认 {@link CircuitBreakerPolicy}
     */
    public static CircuitBreakerPolicy defaults() {
        return new CircuitBreakerPolicy(
                100,
                20,
                50F,
                50F,
                Duration.ofSeconds(2),
                Duration.ofSeconds(30),
                5);
    }
}
