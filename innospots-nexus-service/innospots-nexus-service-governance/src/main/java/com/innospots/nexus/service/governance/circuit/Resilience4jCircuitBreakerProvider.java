package com.innospots.nexus.service.governance.circuit;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.CircuitBreakerProvider;
import com.innospots.nexus.service.contract.governance.CircuitPermit;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.governance.config.CircuitBreakerPolicy;
import com.innospots.nexus.service.governance.config.GovernanceConfig;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;

/**
 * 默认 {@link CircuitBreakerProvider}：按 {@code policyKey} 缓存
 * {@link io.github.resilience4j.circuitbreaker.CircuitBreaker} 实例。
 *
 * <p><strong>生命周期</strong>：{@link #acquire(String)} 调用 {@code acquirePermission()}；
 * OPEN 时抛 {@link com.innospots.nexus.service.contract.status.ServiceStatusCode#CIRCUIT_OPEN}。
 * 返回的 {@link CircuitPermit} 在 finish 时根据
 * {@link #shouldRecordFailure(com.innospots.nexus.service.contract.invocation.InvocationOutcome)}
 * 决定 {@code onSuccess} 或 {@code onError}。</p>
 *
 * <p><strong>失败分类</strong>：权限、限流、舱壁、客户端取消等不计入下游失败；仅实际执行后的
 * FAILED/TIMED_OUT（且非排除状态码）计入。成功调用显式 {@code onSuccess}。</p>
 *
 * <p><strong>约束</strong>：每个稳定下游一个键；配置缺失为 CONFIG_ERROR。线程安全依赖
 * {@link ConcurrentHashMap} 与 Resilience4j 内部同步。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see CircuitBreakerPolicy
 * @see CircuitBreakerInterceptor
 */
public final class Resilience4jCircuitBreakerProvider implements CircuitBreakerProvider {

    /** 断路策略键 → {@link CircuitBreakerPolicy} 配置源。 */
    private final GovernanceConfig config;

    /** 稳定 {@code policyKey} 到 Resilience4j 断路器实例的一一缓存，禁止按请求动态建键。 */
    private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    /**
     * 创建提供方。
     *
     * @param config 含 {@code circuits} 策略表的治理配置
     */
    public Resilience4jCircuitBreakerProvider(GovernanceConfig config) {
        this.config = Checks.notNull(config, "config");
    }

    /**
     * {@inheritDoc}
     *
     * @param policyKey 断路策略键
     * @return 须在终态 finish 的许可
     */
    @Override
    public CircuitPermit acquire(String policyKey) {
        Checks.notBlank(policyKey, "policyKey");
        CircuitBreaker breaker = breakers.computeIfAbsent(policyKey, this::createBreaker);
        try {
            breaker.acquirePermission();
        } catch (CallNotPermittedException ex) {
            throw NexusException.build(ServiceStatusCode.CIRCUIT_OPEN);
        }
        return outcome -> recordOutcome(breaker, outcome);
    }

    /**
     * 返回指定策略键的断路器状态名，供健康检查与测试使用。
     *
     * @param policyKey 策略键；若尚未创建实例则视为 CLOSED
     * @return {@link io.github.resilience4j.circuitbreaker.CircuitBreaker.State} 名称
     */
    public String state(String policyKey) {
        CircuitBreaker breaker = breakers.get(policyKey);
        if (breaker == null) {
            return CircuitBreaker.State.CLOSED.name();
        }
        return breaker.getState().name();
    }

    private CircuitBreaker createBreaker(String policyKey) {
        CircuitBreakerPolicy policy = config.circuits().get(policyKey);
        if (policy == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        CircuitBreakerConfig breakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(policy.slidingWindowSize())
                .minimumNumberOfCalls(policy.minimumNumberOfCalls())
                .failureRateThreshold(policy.failureRateThreshold())
                .slowCallRateThreshold(policy.slowCallRateThreshold())
                .slowCallDurationThreshold(policy.slowCallDurationThreshold())
                .waitDurationInOpenState(policy.waitDurationInOpenState())
                .permittedNumberOfCallsInHalfOpenState(policy.permittedNumberOfCallsInHalfOpenState())
                .build();
        return CircuitBreaker.of(policyKey, breakerConfig);
    }

    private void recordOutcome(CircuitBreaker breaker, InvocationOutcome outcome) {
        Checks.notNull(outcome, "outcome");
        if (!shouldRecordFailure(outcome)) {
            if (outcome.type() == OutcomeType.SUCCEEDED) {
                breaker.onSuccess(0L, TimeUnit.NANOSECONDS);
            }
            return;
        }
        long durationNanos = Math.max(0L, outcome.duration().toNanos());
        breaker.onError(durationNanos, TimeUnit.NANOSECONDS, new CircuitFailure(outcome.code()));
    }

    /**
     * 判断 outcome 是否应作为下游失败计入断路器（包内测试可访问）。
     *
     * @param outcome 调用终态
     * @return {@code true} 表示应 {@code onError}
     */
    static boolean shouldRecordFailure(InvocationOutcome outcome) {
        if (outcome.type() == OutcomeType.CANCELLED || outcome.type() == OutcomeType.REJECTED) {
            return false;
        }
        if (outcome.type() == OutcomeType.SUCCEEDED) {
            return false;
        }
        String code = outcome.code();
        if (NexusStatusCode.NO_PERMISSION.fullCode().equals(code)
                || NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(code)
                || ServiceStatusCode.CAPACITY_EXHAUSTED.fullCode().equals(code)) {
            return false;
        }
        return outcome.type() == OutcomeType.FAILED || outcome.type() == OutcomeType.TIMED_OUT;
    }

    private static final class CircuitFailure extends RuntimeException {

        private CircuitFailure(String code) {
            super(code);
        }
    }
}
