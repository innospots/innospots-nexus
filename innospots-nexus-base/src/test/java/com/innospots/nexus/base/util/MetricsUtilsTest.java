package com.innospots.nexus.base.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetricsUtilsTest {

    @AfterEach
    void clear() {
        MetricsUtils.clear();
    }

    @Test
    void recordsCountersDurationsAndStatus() {
        MetricsUtils.increment("Api.Request", Map.of("channel", "web"));
        String result = MetricsUtils.recordWithStatus("Api.Request", Map.of("channel", "web"), () -> "ok");

        MetricsSnapshot counter = MetricsUtils.snapshot("Api.Request", Map.of("channel", "web"));
        MetricsSnapshot success = MetricsUtils.snapshot("Api.Request.success", Map.of("channel", "web"));

        assertThat(result).isEqualTo("ok");
        assertThat(counter.count()).isEqualTo(2);
        assertThat(counter.totalNanos()).isGreaterThanOrEqualTo(0);
        assertThat(success.count()).isEqualTo(1);
        assertThat(counter.name()).isEqualTo("api_request");
    }

    @Test
    void recordsFailuresAndRethrowsRuntimeException() {
        assertThatThrownBy(() -> MetricsUtils.recordWithStatus("job.run", () -> {
            throw new IllegalStateException("failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(MetricsUtils.snapshot("job.run.failure").count()).isEqualTo(1);
    }
}
