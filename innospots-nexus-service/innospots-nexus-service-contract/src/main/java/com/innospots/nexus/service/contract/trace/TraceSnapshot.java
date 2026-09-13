package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.base.util.Checks;

/**
 * Immutable W3C-compatible trace identifiers. Disabled tracing uses empty strings, not fake zeros.
 *
 * @param traceId  trace identifier, empty when tracing is off
 * @param spanId   span identifier, empty when tracing is off
 * @param sampled  whether the trace is sampled
 * @author Smars
 * @date 2026/09/13
 * @see TraceProvider
 */
public record TraceSnapshot(String traceId, String spanId, boolean sampled) {

    public TraceSnapshot {
        Checks.notNull(traceId, "traceId");
        Checks.notNull(spanId, "spanId");
    }

    /**
     * Returns an unsampled snapshot with empty identifiers.
     *
     * @return empty snapshot
     */
    public static TraceSnapshot empty() {
        return new TraceSnapshot("", "", false);
    }
}
