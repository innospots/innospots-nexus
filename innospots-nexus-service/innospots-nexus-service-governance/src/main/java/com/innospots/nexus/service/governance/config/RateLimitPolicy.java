package com.innospots.nexus.service.governance.config;

import com.innospots.nexus.base.util.Checks;

/**
 * 单键<strong>令牌桶限流</strong>参数。
 *
 * <p><strong>语义</strong>：桶容量为 {@link #burst()}，按单调时钟以每秒 {@link #refillPerSecond()}
 * 补充令牌，补充后不超过容量。与 {@link com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider}
 * 配合使用。默认 {@link com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider}
 * 为<strong>单 JVM</strong>；装配 {@code store=redis} 时由
 * {@code com.innospots.nexus.service.governance.redis.ratelimit.RedisTokenBucketRateLimitProvider}
 * 在共享 Redis 上累计。</p>
 *
 * <p><strong>示例</strong>：{@code new RateLimitPolicy(10, 1.0)} 表示最多突发 10 次，稳态约 1 QPS。</p>
 *
 * @param burst           桶容量（初始满桶），必须 &gt; 0
 * @param refillPerSecond 每秒补充令牌数，必须 &gt; 0
 * @param dimensionMode   维度组合方式，默认 {@link RateLimitDimensionMode#COMPOSITE}
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.config.GovernanceConfig#rateLimits()
 */
public record RateLimitPolicy(
        /** 令牌桶容量（初始满桶）。 */
        int burst,
        /** 每秒补充令牌数，不超过 {@link #burst()}。 */
        double refillPerSecond,
        /** 限流维度组合模式。 */
        RateLimitDimensionMode dimensionMode) {

    /**
     * 使用默认 {@link RateLimitDimensionMode#COMPOSITE} 创建策略。
     *
     * @param burst           桶容量
     * @param refillPerSecond 每秒补充速率
     */
    public RateLimitPolicy(int burst, double refillPerSecond) {
        this(burst, refillPerSecond, RateLimitDimensionMode.COMPOSITE);
    }

    /**
     * 校验 burst 与 refill 为正。
     */
    public RateLimitPolicy {
        Checks.isTrue(burst > 0, "burst must be positive");
        Checks.isTrue(refillPerSecond > 0D, "refillPerSecond must be positive");
        dimensionMode = dimensionMode == null ? RateLimitDimensionMode.COMPOSITE : dimensionMode;
    }
}
