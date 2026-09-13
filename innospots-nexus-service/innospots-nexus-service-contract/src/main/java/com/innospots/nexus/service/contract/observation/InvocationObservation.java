package com.innospots.nexus.service.contract.observation;

import java.time.Duration;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * Per-invocation metric recorder.
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetricsProvider
 */
public interface InvocationObservation {

    /**
     * Records time to first output.
     *
     * @param latency latency from start
     */
    void firstOutput(Duration latency);

    /**
     * Records additional output volume.
     *
     * @param count item count
     * @param bytes byte count
     */
    void output(long count, long bytes);

    /**
     * Completes the observation.
     *
     * @param outcome terminal outcome
     */
    void finish(InvocationOutcome outcome);
}
