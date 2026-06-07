package com.innospots.nexus.base.util;

import java.util.Map;

/**
 * A point-in-time snapshot of a metrics counter/timer. Captures the
 * metric name, tags, total count, and cumulative duration in nanoseconds.
 */
public record MetricsSnapshot(String name, Map<String, String> tags, long count, long totalNanos) {

    public double totalMillis() {
        return totalNanos / 1_000_000D;
    }
}
