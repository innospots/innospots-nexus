package com.innospots.nexus.service.governance.redis.ratelimit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.RateLimitDecision;
import com.innospots.nexus.service.contract.governance.RateLimitProvider;
import com.innospots.nexus.service.contract.governance.RateLimitRequest;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

/**
 * 基于 Redis 的集群限流 {@link RateLimitProvider}，桶语义与 {@link
 * com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider} 一致（复合维度 AND、非阻塞）。
 *
 * <p>桶状态保存在 Redis Hash（{@code tokens}、{@code ts}），由 Lua 脚本原子 refill/检查/扣减。
 * 键格式：{@code {keyPrefix}{policyKey|dimension}}。{@code ts} 与脚本入参 {@code now} 使用
 * {@link System#currentTimeMillis()} 墙钟毫秒，以便多 JVM 共享同一 Redis 键。</p>
 *
 * <p>Redis 不可用或脚本执行失败时<strong>失败关闭</strong>（抛出 {@link NexusStatusCode#SYSTEM_ERROR}），
 * 不降级为放行。{@link GovernanceConfig#maxRateLimitKeys()} 仅作用于本地 Provider，Redis 路径靠
 * {@link RateLimitRedisSettings#bucketTtlSeconds()} 控制键生命周期。</p>
 */
public final class RedisTokenBucketRateLimitProvider implements RateLimitProvider {

    private final GovernanceConfig config;
    private final RateLimitRedisSettings settings;
    private final RateLimitRedisClient redis;

    /**
     * 创建 Redis 限流提供方。
     *
     * @param config   治理配置（策略表）
     * @param settings Redis 键前缀与 TTL
     * @param redis    Redis 脚本执行端口
     */
    public RedisTokenBucketRateLimitProvider(
            GovernanceConfig config,
            RateLimitRedisSettings settings,
            RateLimitRedisClient redis) {
        this.config = Checks.notNull(config, "config");
        this.settings = Checks.notNull(settings, "settings");
        this.redis = Checks.notNull(redis, "redis");
    }

    @Override
    public RateLimitDecision acquire(RateLimitRequest request) {
        Checks.notNull(request, "request");
        RateLimitPolicy policy = config.rateLimits().get(request.policyKey());
        if (policy == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        List<String> bucketKeys = bucketKeys(request);
        Collections.sort(bucketKeys);
        List<String> redisKeys = new ArrayList<>(bucketKeys.size());
        for (String bucketKey : bucketKeys) {
            redisKeys.add(settings.keyPrefix() + bucketKey);
        }
        long nowMillis = System.currentTimeMillis();
        boolean allowed;
        try {
            allowed = redis.tryAcquire(
                    redisKeys,
                    policy.burst(),
                    policy.refillPerSecond(),
                    request.cost(),
                    nowMillis,
                    settings.bucketTtlSeconds());
        } catch (RuntimeException ex) {
            throw NexusException.build(NexusStatusCode.SYSTEM_ERROR, ex);
        }
        if (allowed) {
            return new RateLimitDecision(true, Duration.ZERO);
        }
        return new RateLimitDecision(false, estimateRetryAfter(policy, request.cost()));
    }

    private static List<String> bucketKeys(RateLimitRequest request) {
        List<String> keys = new ArrayList<>();
        if (request.dimensions().isEmpty()) {
            keys.add(request.policyKey());
            return keys;
        }
        for (String dimension : request.dimensions()) {
            keys.add(request.policyKey() + "|" + dimension);
        }
        return keys;
    }

    private static Duration estimateRetryAfter(RateLimitPolicy policy, int cost) {
        double seconds = cost / policy.refillPerSecond();
        if (seconds <= 0D) {
            return Duration.ZERO;
        }
        return Duration.ofMillis((long) Math.ceil(seconds * 1000D));
    }
}
