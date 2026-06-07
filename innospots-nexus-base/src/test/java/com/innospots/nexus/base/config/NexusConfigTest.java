package com.innospots.nexus.base.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NexusConfigTest {

    @Test
    void readsTypedValuesWithDefaults() {
        NexusConfig config = NexusConfig.of(Map.of(
                "name", "nexus",
                "enabled", "true",
                "size", "42"
        ));

        assertThat(config.get("name")).contains("nexus");
        assertThat(config.getBoolean("enabled", false)).isTrue();
        assertThat(config.getInt("size", 0)).isEqualTo(42);
        assertThat(config.get("missing", "fallback")).isEqualTo("fallback");
    }

    @Test
    void rejectsBlankKeys() {
        assertThatThrownBy(() -> NexusConfig.of(Map.of(" ", "bad")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
