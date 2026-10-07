package com.innospots.nexus.service.observability.trace;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.trace.TraceHandle;
import com.innospots.nexus.service.contract.trace.TraceProvider;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

/**
 * 基于 OpenTelemetry API 的 {@link TraceProvider}。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class OpenTelemetryTraceProvider implements TraceProvider {

    private final Tracer tracer;
    private final TraceProvider fallback;

    /**
     * 使用 {@code tracer} 创建提供方；为空时回退到 {@link NoOpTraceProvider}。
     *
     * @param tracer OTel tracer
     */
    public OpenTelemetryTraceProvider(Tracer tracer) {
        this.tracer = tracer;
        this.fallback = new NoOpTraceProvider();
    }

    @Override
    public TraceHandle start(InvocationContext invocation, TraceSnapshot parent) {
        if (tracer == null) {
            return fallback.start(invocation, parent);
        }
        Span span = tracer.spanBuilder(invocation.operationId()).startSpan();
        Scope scope = span.makeCurrent();
        return new OpenTelemetryTraceHandle(span, scope);
    }

    private static final class OpenTelemetryTraceHandle implements TraceHandle {

        private final Span span;
        private final Scope scope;
        private boolean finished;

        private OpenTelemetryTraceHandle(Span span, Scope scope) {
            this.span = Checks.notNull(span, "span");
            this.scope = Checks.notNull(scope, "scope");
        }

        @Override
        public TraceSnapshot snapshot() {
            if (finished || !span.getSpanContext().isValid()) {
                return TraceSnapshot.empty();
            }
            return new TraceSnapshot(
                    span.getSpanContext().getTraceId(),
                    span.getSpanContext().getSpanId(),
                    span.getSpanContext().isSampled());
        }

        @Override
        public void finish(InvocationOutcome outcome) {
            if (finished) {
                return;
            }
            finished = true;
            scope.close();
            span.end();
        }
    }
}
