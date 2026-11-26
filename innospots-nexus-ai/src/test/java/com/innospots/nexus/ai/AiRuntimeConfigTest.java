package com.innospots.nexus.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.ai.config.AiRuntimeConfig;

class AiRuntimeConfigTest {

    @Test
    void defaultsAreValid() {
        AiRuntimeConfig config = AiRuntimeConfig.defaults(Path.of("/tmp/nexus-ai"));
        assertThat(config.startupPolicy()).isEqualTo(AiRuntimeConfig.StartupPolicy.EAGER);
        assertThat(config.bindHost()).isEqualTo("127.0.0.1");
        config.validate();
    }

    @Test
    void rejectsNonLoopbackHost() {
        AiRuntimeConfig config = new AiRuntimeConfig(
                Path.of("/tmp/nexus-ai"), Path.of("/tmp/config"), null, "node", "0.0.0.0", 0,
                java.time.Duration.ofSeconds(1), java.time.Duration.ofSeconds(1), 1,
                AiRuntimeConfig.StartupPolicy.EAGER, java.time.Duration.ofSeconds(1),
                java.time.Duration.ofMillis(100), java.time.Duration.ofMillis(100), 3,
                false, null, java.util.Map.of());
        assertThatThrownBy(config::validate).isInstanceOf(IllegalArgumentException.class);
    }
}
