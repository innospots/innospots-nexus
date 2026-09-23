package com.innospots.nexus.service.contract.governance;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;

/**
 * 限流判定结果。
 *
 * <p><strong>用途</strong>：表达一次 {@link RateLimitProvider#acquire(RateLimitRequest)} 的结论。
 * 拦截器在 {@code allowed == false} 时短路调用链；HTTP 层可将 {@code retryAfter} 映射为
 * {@code Retry-After} 响应头（秒级向上取整，至少 1 秒，由 adapter 负责）。</p>
 *
 * <p><strong>约束</strong>：允许通过时 {@code retryAfter} 应为 {@link Duration#ZERO}；
 * 拒绝时可为估算的最早可重试等待时间，但 Provider 仍不得阻塞等待该时长。</p>
 *
 * @param allowed    为 {@code true} 表示配额已扣减且可继续执行拦截器链与业务
 * @param retryAfter 建议客户端等待时长；允许时为零，拒绝时非负
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitProvider
 */
public record RateLimitDecision(
        /** {@code true} 表示配额已扣减且调用可继续；{@code false} 时应拒绝并可选返回 {@code retryAfter}。 */
        boolean allowed,
        /** 建议客户端等待时长；允许时为 {@link Duration#ZERO}，拒绝时由 Provider 估算。 */
        Duration retryAfter) {

    /**
     * 校验决策不变量。
     *
     * @param allowed    是否允许
     * @param retryAfter 重试间隔，不可为 {@code null} 或负数
     */
    public RateLimitDecision {
        Checks.notNull(retryAfter, "retryAfter");
        Checks.isTrue(!retryAfter.isNegative(), "retryAfter must not be negative");
    }
}
