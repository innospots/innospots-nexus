package com.innospots.nexus.service.observability.metric;

import java.time.Duration;

import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.observation.InvocationObservation;
import com.innospots.nexus.service.contract.observation.MetricsProvider;

/**
 * 无 MeterRegistry 时的指标回退。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class NoOpMetricsProvider implements MetricsProvider {

    @Override
    public InvocationObservation begin(InvocationContext invocation) {
        return new NoOpInvocationObservation();
    }

    private static final class NoOpInvocationObservation implements InvocationObservation {

        @Override
        public void firstOutput(Duration latency) {
        }

        @Override
        public void output(long count, long bytes) {
        }

        @Override
        public void finish(InvocationOutcome outcome) {
        }
    }
}
