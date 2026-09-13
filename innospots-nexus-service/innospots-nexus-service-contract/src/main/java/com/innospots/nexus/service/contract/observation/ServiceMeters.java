package com.innospots.nexus.service.contract.observation;

import java.time.Duration;
import java.util.Map;

/**
 * Business-facing meters. Names must be registered; high-cardinality tags are rejected.
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetricsProvider
 */
public interface ServiceMeters {

    /**
     * Increments a registered counter.
     *
     * @param name registered meter name
     */
    void increment(String name);

    /**
     * Increments a registered counter with allowed tags.
     *
     * @param name registered meter name
     * @param tags allowed tag map
     */
    void increment(String name, Map<String, String> tags);

    /**
     * Records a duration against a registered timer.
     *
     * @param name     registered meter name
     * @param duration duration
     */
    void record(String name, Duration duration);
}
