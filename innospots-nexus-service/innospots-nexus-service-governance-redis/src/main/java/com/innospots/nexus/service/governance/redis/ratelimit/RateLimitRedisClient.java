package com.innospots.nexus.service.governance.redis.ratelimit;

import java.util.List;

/**
 * 执行限流 Lua 脚本的 Redis 端口（便于单测替换为内存实现）。
 */
public interface RateLimitRedisClient {

    /**
     * 原子执行多桶令牌扣减脚本。
     *
     * @param redisKeys   完整 Redis 键列表
     * @param capacity    桶容量
     * @param refillRate  每秒补充令牌
     * @param cost        本次消耗
     * @param nowMillis   当前时间毫秒（单调即可）
     * @param ttlSeconds  键过期秒数
     * @return {@code true} 允许并已扣减
     */
    boolean tryAcquire(
            List<String> redisKeys,
            double capacity,
            double refillRate,
            int cost,
            long nowMillis,
            long ttlSeconds);
}
