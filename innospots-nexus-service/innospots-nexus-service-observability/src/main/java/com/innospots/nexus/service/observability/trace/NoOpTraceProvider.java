package com.innospots.nexus.service.observability.trace;

import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.trace.TraceHandle;
import com.innospots.nexus.service.contract.trace.TraceProvider;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

/**
 * 无 SDK 时的安全追踪回退。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class NoOpTraceProvider implements TraceProvider {

    @Override
    public TraceHandle start(InvocationContext invocation, TraceSnapshot parent) {
        return new NoOpTraceHandle();
    }

    private static final class NoOpTraceHandle implements TraceHandle {

        @Override
        public TraceSnapshot snapshot() {
            return TraceSnapshot.empty();
        }

        @Override
        public void finish(InvocationOutcome outcome) {
        }
    }
}
