package com.innospots.nexus.service.governance.redis.ratelimit;

/**
 * Redis 限流 Lua 脚本常量。
 */
final class RateLimitRedisScripts {

    /**
     * 对多个桶键执行 refill → 全量检查 → 扣减；任一不足则整次拒绝且不扣减。
     *
     * <p>KEYS[1..n] 桶键；ARGV: capacity, refillPerSecond, cost, nowMillis, ttlSeconds。</p>
     */
    static final String MULTI_BUCKET_ACQUIRE = """
            local capacity = tonumber(ARGV[1])
            local rate = tonumber(ARGV[2])
            local cost = tonumber(ARGV[3])
            local now = tonumber(ARGV[4])
            local ttl = tonumber(ARGV[5])
            local n = #KEYS
            local new_tokens = {}
            for i = 1, n do
              local key = KEYS[i]
              local tokens = tonumber(redis.call('HGET', key, 'tokens'))
              local ts = tonumber(redis.call('HGET', key, 'ts'))
              if tokens == nil then
                tokens = capacity
                ts = now
              else
                local elapsed = (now - ts) / 1000.0
                if elapsed > 0 then
                  tokens = math.min(capacity, tokens + elapsed * rate)
                  ts = now
                end
              end
              new_tokens[i] = tokens
              if tokens < cost then
                return 0
              end
            end
            for i = 1, n do
              local key = KEYS[i]
              local tokens = new_tokens[i] - cost
              redis.call('HSET', key, 'tokens', tokens, 'ts', now)
              redis.call('EXPIRE', key, ttl)
            end
            return 1
            """;

    private RateLimitRedisScripts() {
    }
}
