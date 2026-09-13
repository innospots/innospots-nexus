package com.innospots.nexus.service.runtime.invocation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.AttributeKey;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;

/**
 * Runs interceptors then business for sync, async, and stream invocations.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInterceptor
 * @see InvocationContext
 */
public final class InvocationEngine {

    /**
     * Optional attribute marking {@link ExecutionMode#NON_BLOCKING} invocations.
     */
    public static final AttributeKey<ExecutionMode> EXECUTION_MODE =
            new AttributeKey<>("runtime.executionMode", ExecutionMode.class);

    private static final Logger log = LoggerFactory.getLogger(InvocationEngine.class);

    private final List<ServiceInterceptor> interceptors;
    private final ThreadBoundServiceContext contexts;

    /**
     * Creates an engine with interceptors sorted by order then id.
     *
     * @param interceptors interceptors to run
     * @param contexts     thread-bound context accessor
     */
    public InvocationEngine(List<ServiceInterceptor> interceptors, ThreadBoundServiceContext contexts) {
        Checks.notNull(interceptors, "interceptors");
        Checks.notNull(contexts, "contexts");
        List<ServiceInterceptor> sorted = interceptors.stream()
                .sorted(Comparator.comparingInt(ServiceInterceptor::order).thenComparing(ServiceInterceptor::id))
                .toList();
        Set<String> identities = new HashSet<>();
        for (ServiceInterceptor interceptor : sorted) {
            Checks.notBlank(interceptor.id(), "interceptor id");
            if (!identities.add(interceptor.id())) {
                throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
            }
        }
        this.interceptors = sorted;
        this.contexts = contexts;
    }

    /**
     * Invokes a blocking supplier on a worker. {@link ExecutionMode#NON_BLOCKING} is rejected.
     *
     * @param context  invocation
     * @param business business supplier
     * @param <T>      result type
     * @return business result
     */
    public <T> T invokeSync(InvocationContext context, Supplier<T> business) {
        Checks.notNull(context, "context");
        Checks.notNull(business, "business");
        ExecutionMode mode = context.service().attributes()
                .find(EXECUTION_MODE)
                .orElse(ExecutionMode.BLOCKING);
        if (mode == ExecutionMode.NON_BLOCKING) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        Instant started = Instant.now();
        InvocationControl control = new InvocationControl();
        ContextSnapshot snapshot = contexts.install(context.service());
        List<InvocationLease> leases = new ArrayList<>();
        try {
            enter(context, leases);
            T result = business.get();
            finishAll(control, leases, outcome(OutcomeType.SUCCEEDED, NexusStatusCode.SUCCESS.fullCode(), started));
            return result;
        } catch (RuntimeException ex) {
            NexusException failure = asNexus(ex);
            finishAll(control, leases, outcome(failureType(failure), failure.code(), started));
            throw failure;
        } finally {
            contexts.restore(snapshot);
        }
    }

    /**
     * Invokes an async supplier. The supplier is called after interceptors enter.
     *
     * @param context  invocation
     * @param business stage supplier
     * @param <T>      result type
     * @return business stage
     */
    public <T> CompletionStage<T> invokeAsync(InvocationContext context, Supplier<CompletionStage<T>> business) {
        Checks.notNull(context, "context");
        Checks.notNull(business, "business");
        Instant started = Instant.now();
        InvocationControl control = new InvocationControl();
        ContextSnapshot snapshot = contexts.install(context.service());
        List<InvocationLease> leases = new ArrayList<>();
        try {
            enter(context, leases);
            CompletionStage<T> stage = business.get();
            Checks.notNull(stage, "business stage");
            return stage.whenComplete((value, error) -> {
                ContextSnapshot inner = contexts.install(context.service());
                try {
                    if (error == null) {
                        finishAll(control, leases, outcome(OutcomeType.SUCCEEDED, NexusStatusCode.SUCCESS.fullCode(), started));
                    } else {
                        NexusException failure = asNexus(error);
                        finishAll(control, leases, outcome(failureType(failure), failure.code(), started));
                    }
                } finally {
                    contexts.restore(inner);
                }
            });
        } catch (RuntimeException ex) {
            NexusException failure = asNexus(ex);
            finishAll(control, leases, outcome(failureType(failure), failure.code(), started));
            throw failure;
        } finally {
            contexts.restore(snapshot);
        }
    }

    /**
     * Invokes a stream supplier lazily on first subscribe. A second subscribe is rejected.
     *
     * @param context  invocation
     * @param business publisher supplier
     * @param <T>      item type
     * @return publisher
     */
    public <T> Flow.Publisher<T> invokeStream(InvocationContext context, Supplier<Flow.Publisher<T>> business) {
        Checks.notNull(context, "context");
        Checks.notNull(business, "business");
        AtomicBoolean subscribed = new AtomicBoolean();
        return subscriber -> {
            Checks.notNull(subscriber, "subscriber");
            if (!subscribed.compareAndSet(false, true)) {
                throw NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED);
            }
            Instant started = Instant.now();
            InvocationControl control = new InvocationControl();
            ContextSnapshot snapshot = contexts.install(context.service());
            List<InvocationLease> leases = new ArrayList<>();
            try {
                enter(context, leases);
                Flow.Publisher<T> source = business.get();
                Checks.notNull(source, "business publisher");
                source.subscribe(new CompletingSubscriber<>(subscriber, control, leases, started, context));
            } catch (RuntimeException ex) {
                NexusException failure = asNexus(ex);
                finishAll(control, leases, outcome(failureType(failure), failure.code(), started));
                throw failure;
            } finally {
                contexts.restore(snapshot);
            }
        };
    }

    private void enter(InvocationContext context, List<InvocationLease> leases) {
        for (ServiceInterceptor interceptor : interceptors) {
            InvocationLease lease = Checks.notNull(join(interceptor.enter(context)), "interceptor lease");
            leases.add(once(lease));
        }
    }

    private void finishAll(InvocationControl control, List<InvocationLease> leases, InvocationOutcome outcome) {
        control.completeLogical(outcome);
        control.terminateWork();
        for (int index = leases.size() - 1; index >= 0; index--) {
            try {
                join(leases.get(index).finish(outcome));
            } catch (RuntimeException ex) {
                log.warn("Invocation lease finish failed", ex);
            }
        }
    }

    private static InvocationLease once(InvocationLease lease) {
        AtomicBoolean finished = new AtomicBoolean();
        return outcome -> {
            if (!finished.compareAndSet(false, true)) {
                return CompletableFuture.completedFuture(null);
            }
            return lease.finish(outcome);
        };
    }

    private static <T> T join(CompletionStage<T> stage) {
        try {
            return Checks.notNull(stage, "stage").toCompletableFuture().join();
        } catch (CompletionException ex) {
            throw asNexus(ex);
        }
    }

    private static NexusException asNexus(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        if (current instanceof NexusException nexus) {
            return nexus;
        }
        if (current instanceof Error error) {
            throw error;
        }
        if (current instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return NexusException.build(NexusStatusCode.SYSTEM_ERROR, current);
    }

    private static OutcomeType failureType(NexusException failure) {
        if (ServiceStatusCode.DEADLINE_EXCEEDED.fullCode().equals(failure.code())) {
            return OutcomeType.TIMED_OUT;
        }
        if (ServiceStatusCode.OPERATION_CANCELLED.fullCode().equals(failure.code())) {
            return OutcomeType.CANCELLED;
        }
        if (NexusStatusCode.NO_PERMISSION.fullCode().equals(failure.code())
                || NexusStatusCode.AUTHENTICATION_FAILED.fullCode().equals(failure.code())
                || NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(failure.code())) {
            return OutcomeType.REJECTED;
        }
        return OutcomeType.FAILED;
    }

    private static InvocationOutcome outcome(OutcomeType type, String code, Instant started) {
        Duration duration = Duration.between(started, Instant.now());
        if (duration.isNegative()) {
            duration = Duration.ZERO;
        }
        return new InvocationOutcome(type, code, Instant.now(), duration, 0, 0);
    }

    private final class CompletingSubscriber<T> implements Flow.Subscriber<T> {

        private final Flow.Subscriber<? super T> downstream;
        private final InvocationControl control;
        private final List<InvocationLease> leases;
        private final Instant started;
        private final InvocationContext context;
        private final AtomicBoolean terminal = new AtomicBoolean();

        private CompletingSubscriber(
                Flow.Subscriber<? super T> downstream,
                InvocationControl control,
                List<InvocationLease> leases,
                Instant started,
                InvocationContext context) {
            this.downstream = downstream;
            this.control = control;
            this.leases = leases;
            this.started = started;
            this.context = context;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            downstream.onSubscribe(subscription);
        }

        @Override
        public void onNext(T item) {
            ContextSnapshot inner = contexts.install(context.service());
            try {
                downstream.onNext(item);
            } finally {
                contexts.restore(inner);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            ContextSnapshot inner = contexts.install(context.service());
            try {
                if (terminal.compareAndSet(false, true)) {
                    NexusException failure = asNexus(throwable);
                    finishAll(control, leases, outcome(failureType(failure), failure.code(), started));
                }
                downstream.onError(throwable);
            } finally {
                contexts.restore(inner);
            }
        }

        @Override
        public void onComplete() {
            ContextSnapshot inner = contexts.install(context.service());
            try {
                if (terminal.compareAndSet(false, true)) {
                    finishAll(control, leases, outcome(OutcomeType.SUCCEEDED, NexusStatusCode.SUCCESS.fullCode(), started));
                }
                downstream.onComplete();
            } finally {
                contexts.restore(inner);
            }
        }
    }
}
