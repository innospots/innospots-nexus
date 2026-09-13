package com.innospots.nexus.service.contract.policy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeveloperExperienceContractsTest {

    @Test
    void doesNotExposeForbiddenEnableOrCrossCuttingAnnotations() {
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.EnableTracing");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.Tracing");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.Logging");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.Metrics");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.RequestContextEnabled");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.Bulkheaded");
        assertMissing("com.innospots.nexus.service.contract.policy.annotation.Timed");
        assertMissing("com.innospots.nexus.service.contract.observation.EnableTracing");
        assertMissing("com.innospots.nexus.service.contract.observation.Logging");
        assertMissing("com.innospots.nexus.service.contract.observation.Metrics");
    }

    private static void assertMissing(String className) {
        assertThatThrownBy(() -> Class.forName(className))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
