package com.innospots.nexus.base.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EnvUtilsTest {

    @AfterEach
    void clear() {
        EnvUtils.clear("NEXUS_ENV_TEST");
        EnvUtils.clear("NEXUS_ENV_BATCH");
    }

    @Test
    void readsOverridesBeforeSystemEnvironment() {
        EnvUtils.set("NEXUS_ENV_TEST", "local");

        assertThat(EnvUtils.value("NEXUS_ENV_TEST")).isEqualTo("local");
        assertThat(EnvUtils.value("NEXUS_ENV_TEST", "fallback")).isEqualTo("local");
    }

    @Test
    void supportsBatchOverridesAndFallbacks() {
        EnvUtils.putAll(Map.of("NEXUS_ENV_BATCH", "batch"));

        assertThat(EnvUtils.value("NEXUS_ENV_BATCH")).isEqualTo("batch");
        assertThat(EnvUtils.value("NEXUS_ENV_MISSING", "fallback")).isEqualTo("fallback");
    }
}
