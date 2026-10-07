package com.innospots.nexus.service.governance.redis.ratelimit;

import com.innospots.nexus.base.util.Checks;

/**
 * Redis 限流键命名与连接参数（framework-neutral，由 adapter 装配）。
 *
 * @param keyPrefix  桶键前缀，如 {@code nexus:ratelimit:}
 * @param bucketTtlSeconds  桶 Hash 过期秒数，防止 Redis 键无限增长
 * @author Smars
 * @date 2026/09/22
 */
public record RateLimitRedisSettings(String keyPrefix, long bucketTtlSeconds) {

    public RateLimitRedisSettings {
        Checks.notBlank(keyPrefix, "keyPrefix");
        Checks.isTrue(bucketTtlSeconds > 0, "bucketTtlSeconds must be positive");
    }

    /**
     * 默认设置。
     *
     * @return 默认前缀与 24h TTL
     */
    public static RateLimitRedisSettings defaults() {
        return new RateLimitRedisSettings("nexus:ratelimit:", 86_400L);
    }
}
