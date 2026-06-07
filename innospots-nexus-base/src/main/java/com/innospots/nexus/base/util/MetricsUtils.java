package com.innospots.nexus.base.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Micrometer-based metrics facade. Provides counters, timers, and
 * gauges with automatic name normalization (lowercase, underscore-separated).
 * Supports success/failure/retry counting and duration recording.
 */
public final class MetricsUtils {

    private static final String TIMER_SUFFIX = "_duration";
    private static final String SUCCESS_SUFFIX = "_success";
    private static final String FAILURE_SUFFIX = "_failure";
    private static final String RETRY_SUFFIX = "_retry";
    private static volatile MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private MetricsUtils() {
    }

    public static void initialize(MeterRegistry registry) {
        meterRegistry = Objects.requireNonNull(registry, "MeterRegistry must not be null");
    }

    public static MeterRegistry registry() {
        return meterRegistry;
    }

    public static void increment(String name) {
        increment(name, Map.of(), 1);
    }

    public static void increment(String name, Map<String, String> tags) {
        increment(name, tags, 1);
    }

    public static void increment(String name, Map<String, String> tags, long amount) {
        Counter.builder(normalizeName(name))
                .tags(tags(tags))
                .register(meterRegistry)
                .increment(Math.max(0, amount));
    }

    public static <T> T record(String name, Callable<T> task) {
        return record(name, Map.of(), task);
    }

    public static <T> T record(String name, Map<String, String> tags, Callable<T> task) {
        Timer timer = timer(name, tags);
        try {
            return timer.recordCallable(task);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Metric task failed: " + name, e);
        }
    }

    public static void record(String name, Runnable task) {
        record(name, Map.of(), task);
    }

    public static void record(String name, Map<String, String> tags, Runnable task) {
        timer(name, tags).record(task);
    }

    public static <T> T recordWithStatus(String name, Callable<T> task) {
        return recordWithStatus(name, Map.of(), task);
    }

    public static <T> T recordWithStatus(String name, Map<String, String> tags, Callable<T> task) {
        try {
            T result = record(name, tags, task);
            countSuccess(name, tags);
            return result;
        } catch (RuntimeException e) {
            countFailure(name, tags);
            throw e;
        }
    }

    public static void recordWithStatus(String name, Runnable task) {
        recordWithStatus(name, Map.of(), task);
    }

    public static void recordWithStatus(String name, Map<String, String> tags, Runnable task) {
        recordWithStatus(name, tags, () -> {
            task.run();
            return null;
        });
    }

    public static void recordDuration(String name, Map<String, String> tags, long nanos) {
        timer(name, tags).record(Math.max(0, nanos), TimeUnit.NANOSECONDS);
    }

    public static void recordDuration(String name, Map<String, String> tags, Duration duration) {
        timer(name, tags).record(duration == null ? Duration.ZERO : duration);
    }

    public static void countSuccess(String name, Map<String, String> tags) {
        increment(normalizeName(name) + SUCCESS_SUFFIX, tags);
    }

    public static void countFailure(String name, Map<String, String> tags) {
        increment(normalizeName(name) + FAILURE_SUFFIX, tags);
    }

    public static void countRetry(String name, Map<String, String> tags) {
        increment(normalizeName(name) + RETRY_SUFFIX, tags);
    }

    public static void gauge(String name, Map<String, String> tags, Supplier<Number> supplier) {
        io.micrometer.core.instrument.Gauge.builder(normalizeName(name), supplier, MetricsUtils::gaugeValue)
                .tags(tags(tags))
                .register(meterRegistry);
    }

    public static MetricsSnapshot snapshot(String name) {
        return snapshot(name, Map.of());
    }

    public static MetricsSnapshot snapshot(String name, Map<String, String> tags) {
        String normalizedName = normalizeName(name);
        Map<String, String> normalizedTags = normalizeTags(tags);
        Counter counter = meterRegistry.find(normalizedName).tags(tagsArray(normalizedTags)).counter();
        Timer timer = meterRegistry.find(timerName(normalizedName)).tags(tagsArray(normalizedTags)).timer();
        long counterCount = counter == null ? 0 : Math.round(counter.count());
        long timerCount = timer == null ? 0 : timer.count();
        long totalNanos = timer == null ? 0 : (long) timer.totalTime(TimeUnit.NANOSECONDS);
        return new MetricsSnapshot(normalizedName, normalizedTags, counterCount + timerCount, totalNanos);
    }

    public static void clear() {
        meterRegistry.clear();
    }

    public static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be blank");
        }
        String normalized = name.toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Metric name cannot be normalized: " + name);
        }
        return normalized;
    }

    private static Timer timer(String name, Map<String, String> tags) {
        String normalizedName = normalizeName(name);
        return Timer.builder(timerName(normalizedName))
                .tags(tags(tags))
                .register(meterRegistry);
    }

    private static String timerName(String normalizedName) {
        return normalizedName + TIMER_SUFFIX;
    }

    private static double gaugeValue(Supplier<Number> supplier) {
        Number value = supplier.get();
        return value == null ? 0D : value.doubleValue();
    }

    private static Tags tags(Map<String, String> tags) {
        return Tags.of(normalizeTags(tags).entrySet().stream()
                .map(entry -> io.micrometer.core.instrument.Tag.of(entry.getKey(), entry.getValue()))
                .toList());
    }

    private static String[] tagsArray(Map<String, String> tags) {
        String[] values = new String[tags.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            values[index++] = entry.getKey();
            values[index++] = entry.getValue();
        }
        return values;
    }

    private static Map<String, String> normalizeTags(Map<String, String> tags) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (tags != null) {
            new TreeMap<>(tags).forEach((key, value) ->
                    normalized.put(Objects.toString(key, ""), Objects.toString(value, "")));
        }
        return Map.copyOf(normalized);
    }
}
