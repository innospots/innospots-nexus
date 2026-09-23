package com.innospots.nexus.service.governance.ratelimit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.RateLimitDecision;
import com.innospots.nexus.service.contract.governance.RateLimitProvider;
import com.innospots.nexus.service.contract.governance.RateLimitRequest;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

/**
 * 默认 {@link RateLimitProvider}：<strong>单 JVM</strong> 复合维度令牌桶。
 *
 * <p><strong>算法</strong>：每个桶键独立维护容量 {@link RateLimitPolicy#burst()} 与
 * {@link RateLimitPolicy#refillPerSecond()} 补充；一次请求对<strong>所有</strong>相关桶先 refill、
 * 再检查是否均有足够 {@code cost}，任一不足则<strong>整次拒绝且不扣减</strong>。桶键为
 * {@code policyKey|dimension} 或仅 {@code policyKey}（无维度时）。</p>
 *
 * <p><strong>并发</strong>：对排序后的桶键拼接串 {@code intern()} 作为监视器，避免多桶死锁。
 * 使用 {@link Ticker} 单调纳秒时间，不依赖 {@link System#currentTimeMillis()}。</p>
 *
 * <p><strong>容量保护</strong>：新建桶前若将超过 {@link GovernanceConfig#maxRateLimitKeys()}，
 * 先按 {@link GovernanceConfig#rateLimitIdleTtl()} 淘汰闲置桶；仍满则
 * {@link ServiceStatusCode#CAPACITY_EXHAUSTED}。未知 {@code policyKey} 为
 * {@link NexusStatusCode#CONFIG_ERROR}。</p>
 *
 * <p><strong>使用约束</strong>：非集群限流；N 副本部署时配额按节点相加大致估算。替换为 Redis 等实现时
 * 保持 {@link #acquire(RateLimitRequest)} 非阻塞。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see RateLimitInterceptor
 * @see RateLimitPolicy
 */
public final class LocalTokenBucketProvider implements RateLimitProvider {

    /** 限流策略表与桶容量上限、idle 淘汰 TTL 等全局参数。 */
    private final GovernanceConfig config;

    /** 单调纳秒时钟，用于 refill 与闲置桶淘汰，避免墙钟回拨。 */
    private final Ticker ticker;

    /** 桶键 → 令牌状态；键形如 {@code policyKey|dimension} 或单独 {@code policyKey}。 */
    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    /**
     * 创建提供方。
     *
     * @param config 含 {@code rateLimits} 与桶上限/淘汰 TTL 的治理配置
     * @param ticker 单调时钟，用于 refill 与 idle 淘汰
     */
    public LocalTokenBucketProvider(GovernanceConfig config, Ticker ticker) {
        this.config = Checks.notNull(config, "config");
        this.ticker = Checks.notNull(ticker, "ticker");
    }

    /**
     * {@inheritDoc}
     *
     * <p>拒绝时 {@link RateLimitDecision#retryAfter()} 为各不足桶所需等待时间的最大值（估算）。</p>
     *
     * @param request 限流请求，不可为 {@code null}
     * @return 允许或拒绝决策
     */
    @Override
    public RateLimitDecision acquire(RateLimitRequest request) {
        Checks.notNull(request, "request");
        RateLimitPolicy policy = config.rateLimits().get(request.policyKey());
        if (policy == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        List<String> bucketKeys = bucketKeys(request);
        Collections.sort(bucketKeys);
        Object lock = lockFor(bucketKeys);
        synchronized (lock) {
            ensureCapacityForNewKeys(bucketKeys);
            refill(bucketKeys, policy);
            if (!hasCapacity(bucketKeys, policy, request.cost())) {
                Duration retryAfter = computeRetryAfter(bucketKeys, policy, request.cost());
                return new RateLimitDecision(false, retryAfter);
            }
            deduct(bucketKeys, request.cost());
            touch(bucketKeys);
            return new RateLimitDecision(true, Duration.ZERO);
        }
    }

    /**
     * 返回进程内当前桶条目数，供测试与运维观测（非严格配额指标）。
     *
     * @return {@code buckets} Map 的 size
     */
    public int bucketCount() {
        return buckets.size();
    }

    private List<String> bucketKeys(RateLimitRequest request) {
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

    private Object lockFor(List<String> bucketKeys) {
        return bucketKeys.stream().sorted().reduce("", String::concat).intern();
    }

    private void refill(List<String> bucketKeys, RateLimitPolicy policy) {
        long now = ticker.readNanos();
        for (String key : bucketKeys) {
            BucketState state = buckets.computeIfAbsent(key, ignored -> new BucketState(policy.burst()));
            state.refill(now, policy);
        }
    }

    private boolean hasCapacity(List<String> bucketKeys, RateLimitPolicy policy, int cost) {
        for (String key : bucketKeys) {
            BucketState state = buckets.get(key);
            if (state == null || state.tokens < cost) {
                return false;
            }
        }
        return true;
    }

    private void deduct(List<String> bucketKeys, int cost) {
        for (String key : bucketKeys) {
            BucketState state = buckets.get(key);
            if (state != null) {
                state.tokens -= cost;
            }
        }
    }

    private void touch(List<String> bucketKeys) {
        long now = ticker.readNanos();
        for (String key : bucketKeys) {
            BucketState state = buckets.get(key);
            if (state != null) {
                state.lastAccessNanos = now;
            }
        }
    }

    private Duration computeRetryAfter(List<String> bucketKeys, RateLimitPolicy policy, int cost) {
        long maxWaitNanos = 0L;
        for (String key : bucketKeys) {
            BucketState state = buckets.get(key);
            if (state == null) {
                maxWaitNanos = Math.max(maxWaitNanos, nanosToRefill(cost, policy));
                continue;
            }
            if (state.tokens >= cost) {
                continue;
            }
            double deficit = cost - state.tokens;
            long waitNanos = (long) Math.ceil(deficit / policy.refillPerSecond() * 1_000_000_000L);
            maxWaitNanos = Math.max(maxWaitNanos, waitNanos);
        }
        if (maxWaitNanos <= 0L) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(maxWaitNanos);
    }

    private static long nanosToRefill(int cost, RateLimitPolicy policy) {
        return (long) Math.ceil(cost / policy.refillPerSecond() * 1_000_000_000L);
    }

    private void ensureCapacityForNewKeys(List<String> bucketKeys) {
        long newKeys = bucketKeys.stream().filter(key -> !buckets.containsKey(key)).count();
        if (newKeys == 0L) {
            return;
        }
        if (buckets.size() + newKeys <= config.maxRateLimitKeys()) {
            return;
        }
        evictIdleBuckets();
        if (buckets.size() + newKeys > config.maxRateLimitKeys()) {
            throw NexusException.build(ServiceStatusCode.CAPACITY_EXHAUSTED);
        }
    }

    private void evictIdleBuckets() {
        long cutoff = ticker.readNanos() - config.rateLimitIdleTtl().toNanos();
        buckets.entrySet().removeIf(entry -> entry.getValue().lastAccessNanos < cutoff);
    }

    private static final class BucketState {

        /** 桶容量上限，来自 {@link RateLimitPolicy#burst()}。 */
        private final double capacity;

        /** 当前可用令牌数，扣减与补充均不超过 {@link #capacity}。 */
        private double tokens;

        /** 上次按策略补充令牌时的单调纳秒时间戳。 */
        private long lastRefillNanos;

        /** 上次成功访问（扣减或检查）时的单调纳秒时间戳，用于 idle 淘汰。 */
        private long lastAccessNanos;

        private BucketState(double capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
        }

        private void refill(long now, RateLimitPolicy policy) {
            if (lastRefillNanos == 0L) {
                lastRefillNanos = now;
                lastAccessNanos = now;
                return;
            }
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos <= 0L) {
                return;
            }
            double added = elapsedNanos / 1_000_000_000D * policy.refillPerSecond();
            tokens = Math.min(capacity, tokens + added);
            lastRefillNanos = now;
        }
    }
}
