package com.innospots.nexus.service.governance.redis.ratelimit;

import java.util.List;

import com.innospots.nexus.base.util.Checks;

import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 基于 Lettuce 同步连接的 {@link RateLimitRedisClient}。
 */
public final class LettuceRateLimitRedisClient implements RateLimitRedisClient {

    private final StatefulRedisConnection<String, String> connection;

    /**
     * 创建客户端。
     *
     * @param connection 已建立的 Lettuce 连接（由 adapter 管理生命周期）
     */
    public LettuceRateLimitRedisClient(StatefulRedisConnection<String, String> connection) {
        this.connection = Checks.notNull(connection, "connection");
    }

    @Override
    public boolean tryAcquire(
            List<String> redisKeys,
            double capacity,
            double refillRate,
            int cost,
            long nowMillis,
            long ttlSeconds) {
        Checks.notNull(redisKeys, "redisKeys");
        if (redisKeys.isEmpty()) {
            return true;
        }
        RedisCommands<String, String> commands = connection.sync();
        Object result = commands.eval(
                RateLimitRedisScripts.MULTI_BUCKET_ACQUIRE,
                ScriptOutputType.INTEGER,
                redisKeys.toArray(String[]::new),
                String.valueOf(capacity),
                String.valueOf(refillRate),
                String.valueOf(cost),
                String.valueOf(nowMillis),
                String.valueOf(ttlSeconds));
        if (result instanceof Long allowed) {
            return allowed == 1L;
        }
        if (result instanceof Integer allowed) {
            return allowed == 1;
        }
        return false;
    }
}
