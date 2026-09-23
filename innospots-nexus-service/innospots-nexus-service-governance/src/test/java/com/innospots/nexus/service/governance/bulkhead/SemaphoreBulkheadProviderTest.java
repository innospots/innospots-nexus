package com.innospots.nexus.service.governance.bulkhead;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.governance.BulkheadPermit;
import com.innospots.nexus.service.governance.config.BulkheadPolicy;
import com.innospots.nexus.service.governance.config.GovernanceConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 信号量舱壁无排队、幂等释放与异常路径测试。
 */
class SemaphoreBulkheadProviderTest {

    @Test
    void doesNotQueueWhenCapacityExhausted() {
        SemaphoreBulkheadProvider provider = provider(1);

        Optional<BulkheadPermit> first = provider.tryAcquire("compute");
        Optional<BulkheadPermit> second = provider.tryAcquire("compute");

        assertThat(first).isPresent();
        assertThat(second).isEmpty();
    }

    @Test
    void closeIsIdempotent() {
        SemaphoreBulkheadProvider provider = provider(1);
        BulkheadPermit permit = provider.tryAcquire("compute").orElseThrow();

        permit.close();
        permit.close();

        assertThat(provider.tryAcquire("compute")).isPresent();
    }

    @Test
    void releasesPermitOnExceptionPath() {
        SemaphoreBulkheadProvider provider = provider(1);
        BulkheadPermit permit = provider.tryAcquire("compute").orElseThrow();
        try {
            throw new IllegalStateException("boom");
        } catch (RuntimeException ex) {
            permit.close();
        }
        assertThat(provider.tryAcquire("compute")).isPresent();
    }

    private static SemaphoreBulkheadProvider provider(int maxConcurrent) {
        GovernanceConfig config = new GovernanceConfig(
                Map.of(),
                Map.of("compute", new BulkheadPolicy(maxConcurrent)),
                Map.of(),
                Map.of(),
                100,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        return new SemaphoreBulkheadProvider(config);
    }
}
