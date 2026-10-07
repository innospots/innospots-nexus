package com.innospots.nexus.service.governance.ratelimit;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.governance.RateLimitDecision;
import com.innospots.nexus.service.contract.governance.RateLimitRequest;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 本地令牌桶复合扣减、键容量与 retryAfter 测试。
 */
class LocalTokenBucketProviderTest {

    @Test
    void compositeDimensionsAreAtomic() {
        LocalTokenBucketProvider provider = provider(
                new RateLimitPolicy(10, 0.1D),
                100,
                Duration.ofMinutes(15));
        RateLimitRequest request = new RateLimitRequest("orders", List.of("tenant:a", "tenant:b"), 8);

        assertThat(provider.acquire(request).allowed()).isTrue();
        assertThat(provider.acquire(new RateLimitRequest("orders", List.of("tenant:a", "tenant:b"), 3)).allowed())
                .isFalse();
        assertThat(provider.acquire(new RateLimitRequest("orders", List.of("tenant:a", "tenant:b"), 2)).allowed())
                .isTrue();
    }

    @Test
    void rejectsWhenKeyCapacityExhausted() {
        GovernanceConfig config = new GovernanceConfig(
                Map.of(
                        "a", new RateLimitPolicy(4, 1D),
                        "b", new RateLimitPolicy(4, 1D)),
                Map.of(),
                Map.of(),
                Map.of(),
                1,
                Duration.ofHours(1),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        LocalTokenBucketProvider provider = new LocalTokenBucketProvider(config, Ticker.system());

        provider.acquire(new RateLimitRequest("a", List.of("one"), 1));

        assertThatThrownBy(() -> provider.acquire(new RateLimitRequest("b", List.of("two"), 1)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.CAPACITY_EXHAUSTED.fullCode());
    }

    @Test
    void deniedDecisionIncludesRetryAfter() {
        LocalTokenBucketProvider provider = provider(new RateLimitPolicy(2, 1D), 100, Duration.ofMinutes(15));
        provider.acquire(new RateLimitRequest("api", List.of("tenant:1"), 2));

        RateLimitDecision denied = provider.acquire(new RateLimitRequest("api", List.of("tenant:1"), 1));

        assertThat(denied.allowed()).isFalse();
        assertThat(denied.retryAfter()).isGreaterThan(Duration.ZERO);
    }

    private static LocalTokenBucketProvider provider(
            RateLimitPolicy policy,
            int maxKeys,
            Duration idleTtl) {
        GovernanceConfig config = new GovernanceConfig(
                Map.of("orders", policy, "api", policy),
                Map.of(),
                Map.of(),
                Map.of(),
                maxKeys,
                idleTtl,
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        AtomicLong clock = new AtomicLong(0L);
        Ticker ticker = clock::get;
        return new LocalTokenBucketProvider(config, ticker);
    }
}
