package com.innospots.nexus.service.governance.redis.ratelimit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进程内模拟 Redis Lua 语义，供单测使用。
 */
final class InMemoryRateLimitRedisClient implements RateLimitRedisClient {

    private final Map<String, BucketState> buckets = new HashMap<>();

    @Override
    public boolean tryAcquire(
            List<String> redisKeys,
            double capacity,
            double refillRate,
            int cost,
            long nowMillis,
            long ttlSeconds) {
        if (redisKeys.isEmpty()) {
            return true;
        }
        double[] refilled = new double[redisKeys.size()];
        for (int i = 0; i < redisKeys.size(); i++) {
            BucketState state = buckets.computeIfAbsent(redisKeys.get(i), ignored -> new BucketState(capacity));
            refilled[i] = state.refill(nowMillis, capacity, refillRate);
            if (refilled[i] < cost) {
                return false;
            }
        }
        for (int i = 0; i < redisKeys.size(); i++) {
            BucketState state = buckets.get(redisKeys.get(i));
            state.tokens = refilled[i] - cost;
            state.ts = nowMillis;
        }
        return true;
    }

    private static final class BucketState {

        private double tokens;
        private long ts;

        private BucketState(double capacity) {
            this.tokens = capacity;
        }

        private double refill(long now, double capacity, double refillRate) {
            if (ts == 0L) {
                ts = now;
                tokens = capacity;
                return tokens;
            }
            double elapsed = (now - ts) / 1000.0D;
            if (elapsed > 0D) {
                tokens = Math.min(capacity, tokens + elapsed * refillRate);
                ts = now;
            }
            return tokens;
        }
    }
}
