package com.innospots.nexus.service.governance.redis.ratelimit;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.governance.RateLimitRequest;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitDimensionMode;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Redis 限流 Provider 单测（内存 Redis 模拟）。
 */
class RedisTokenBucketRateLimitProviderTest {

    @Test
    void rejectsSecondAcquireOnSameCustomerBucket() {
        GovernanceConfig config = new GovernanceConfig(
                Map.of("ws", new RateLimitPolicy(1, 1.0D, RateLimitDimensionMode.CUSTOMER)),
                Map.of(),
                Map.of(),
                Map.of(),
                10_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        RedisTokenBucketRateLimitProvider provider = new RedisTokenBucketRateLimitProvider(
                config,
                RateLimitRedisSettings.defaults(),
                new InMemoryRateLimitRedisClient());
        RateLimitRequest request = new RateLimitRequest("ws", List.of("customer:c-1"), 1);
        assertThat(provider.acquire(request).allowed()).isTrue();
        assertThat(provider.acquire(request).allowed()).isFalse();
    }

    @Test
    void failsClosedWhenRedisClientThrows() {
        GovernanceConfig config = new GovernanceConfig(
                Map.of("api", new RateLimitPolicy(10, 1.0D)),
                Map.of(),
                Map.of(),
                Map.of(),
                10_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        RateLimitRedisClient failing = new RateLimitRedisClient() {
            @Override
            public boolean tryAcquire(
                    java.util.List<String> redisKeys,
                    double capacity,
                    double refillRate,
                    int cost,
                    long nowMillis,
                    long ttlSeconds) {
                throw new IllegalStateException("redis down");
            }
        };
        RedisTokenBucketRateLimitProvider provider =
                new RedisTokenBucketRateLimitProvider(config, RateLimitRedisSettings.defaults(), failing);
        RateLimitRequest request = new RateLimitRequest("api", List.of("principal:p1"), 1);
        assertThatThrownBy(() -> provider.acquire(request))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.SYSTEM_ERROR.fullCode());
    }
}
