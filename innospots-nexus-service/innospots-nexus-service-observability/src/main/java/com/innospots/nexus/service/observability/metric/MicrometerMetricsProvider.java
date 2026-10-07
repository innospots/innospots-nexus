package com.innospots.nexus.service.observability.metric;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.observation.InvocationObservation;
import com.innospots.nexus.service.contract.observation.MetricsProvider;
import com.innospots.nexus.service.contract.observation.ServiceMeters;
import com.innospots.nexus.service.observability.config.ObservabilityConfig;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * 基于 Micrometer 的 {@link MetricsProvider} 与 {@link ServiceMeters} 实现。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class MicrometerMetricsProvider implements MetricsProvider, ServiceMeters {

    private static final String REQUEST_COUNTER = "service.http.requests";
    private static final String REQUEST_TIMER = "service.http.duration";

    private final MeterRegistry registry;
    private final ObservabilityConfig config;
    private final MetricsProvider fallback;

    /**
     * 创建提供方。
     *
     * @param registry Micrometer 注册表
     * @param config   观测配置
     */
    public MicrometerMetricsProvider(MeterRegistry registry, ObservabilityConfig config) {
        this.registry = registry;
        this.config = Checks.notNull(config, "config");
        this.fallback = new NoOpMetricsProvider();
    }

    @Override
    public InvocationObservation begin(InvocationContext invocation) {
        if (registry == null) {
            return fallback.begin(invocation);
        }
        return new MicrometerInvocationObservation(registry, invocation);
    }

    @Override
    public void increment(String name) {
        increment(name, Map.of());
    }

    @Override
    public void increment(String name, Map<String, String> tags) {
        if (registry == null) {
            return;
        }
        requireRegisteredBusinessMeter(name, tags);
        Counter.builder(name).tags(flattenTags(tags)).register(registry).increment();
    }

    @Override
    public void record(String name, Duration duration) {
        if (registry == null) {
            return;
        }
        requireRegisteredBusinessMeter(name, Map.of());
        Timer.builder(name).register(registry).record(duration);
    }

    private void requireRegisteredBusinessMeter(String name, Map<String, String> tags) {
        if (!config.businessMeterNames().contains(name)) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        Set<String> allowedTags = config.businessMeterTagWhitelist().getOrDefault(name, Set.of());
        for (String key : tags.keySet()) {
            if (!allowedTags.contains(key)) {
                throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
            }
        }
    }

    private static String[] flattenTags(Map<String, String> tags) {
        String[] flattened = new String[tags.size() * 2];
        int index = 0;
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            flattened[index++] = entry.getKey();
            flattened[index++] = entry.getValue();
        }
        return flattened;
    }

    private final class MicrometerInvocationObservation implements InvocationObservation {

        private final Timer.Sample sample;
        private final AtomicBoolean finished = new AtomicBoolean(false);

        private MicrometerInvocationObservation(MeterRegistry registry, InvocationContext invocation) {
            this.sample = Timer.start(registry);
            Counter.builder(REQUEST_COUNTER)
                    .tag("operation", sanitize(invocation.operationId()))
                    .register(registry)
                    .increment();
        }

        @Override
        public void firstOutput(Duration latency) {
        }

        @Override
        public void output(long count, long bytes) {
        }

        @Override
        public void finish(InvocationOutcome outcome) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            sample.stop(Timer.builder(REQUEST_TIMER)
                    .tag("operation", sanitize(outcome.code()))
                    .tag("result", outcome.type().name())
                    .register(registry));
        }

        private String sanitize(String value) {
            if (value == null || value.isBlank()) {
                return "unknown";
            }
            return value;
        }
    }
}
