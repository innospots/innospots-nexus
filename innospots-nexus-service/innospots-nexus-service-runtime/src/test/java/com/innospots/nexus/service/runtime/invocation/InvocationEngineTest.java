package com.innospots.nexus.service.runtime.invocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Flow;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.policy.AuditMode;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.policy.ResponseProfile;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 调用引擎同步、异步与流式路径及拦截器链行为测试。
 */
class InvocationEngineTest {

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
    private final List<String> events = new ArrayList<>();

    @Test
    void invokeSyncRunsBusinessBetweenEnterAndReverseFinish() {
        RecordingInterceptor first = new RecordingInterceptor("first", 10, events, null);
        RecordingInterceptor second = new RecordingInterceptor("second", 20, events, null);
        InvocationEngine engine = new InvocationEngine(List.of(second, first), contexts);

        String result = engine.invokeSync(context(), () -> {
            events.add("business");
            assertThat(contexts.requireCurrent().requestId()).isEqualTo("req-1");
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(events).containsExactly("enter:first", "enter:second", "business", "finish:second", "finish:first");
        assertThat(contexts.current()).isEmpty();
    }

    @Test
    void interceptorFailureSkipsBusinessAndFinishesAcquiredLeases() {
        RecordingInterceptor first = new RecordingInterceptor("first", 10, events, null);
        RecordingInterceptor failing = new RecordingInterceptor(
                "failing",
                20,
                events,
                NexusException.build(NexusStatusCode.NO_PERMISSION));
        AtomicBoolean business = new AtomicBoolean();
        InvocationEngine engine = new InvocationEngine(List.of(first, failing), contexts);

        assertThatThrownBy(() -> engine.invokeSync(context(), () -> {
            business.set(true);
            return "nope";
        }))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());

        assertThat(business).isFalse();
        assertThat(events).containsExactly("enter:first", "enter:failing", "finish:first");
    }

    @Test
    void leaseFinishIsIdempotent() {
        AtomicInteger finishes = new AtomicInteger();
        ServiceInterceptor interceptor = new ServiceInterceptor() {
            @Override
            public String id() {
                return "once";
            }

            @Override
            public int order() {
                return 0;
            }

            @Override
            public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
                return CompletableFuture.completedFuture(outcome -> {
                    finishes.incrementAndGet();
                    return CompletableFuture.completedFuture(null);
                });
            }
        };
        InvocationEngine engine = new InvocationEngine(List.of(interceptor), contexts);
        engine.invokeSync(context(), () -> "ok");
        assertThat(finishes).hasValue(1);
    }

    @Test
    void invokeAsyncAndStreamSucceed() throws Exception {
        InvocationEngine engine = new InvocationEngine(List.of(), contexts);
        CompletionStage<String> async = engine.invokeAsync(context(), () -> CompletableFuture.completedFuture("async"));
        assertThat(async.toCompletableFuture().get()).isEqualTo("async");

        Flow.Publisher<String> publisher = engine.invokeStream(context(), () -> subscriber ->
                subscriber.onSubscribe(new Flow.Subscription() {
                    @Override
                    public void request(long n) {
                        subscriber.onNext("stream");
                        subscriber.onComplete();
                    }

                    @Override
                    public void cancel() {
                    }
                }));
        CompletableFuture<String> item = new CompletableFuture<>();
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(String value) {
                item.complete(value);
            }

            @Override
            public void onError(Throwable throwable) {
                item.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
            }
        });
        assertThat(item.get()).isEqualTo("stream");
        assertThatThrownBy(() -> publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
            }

            @Override
            public void onNext(String item) {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onComplete() {
            }
        }))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.LIFECYCLE_CLOSED.fullCode());
    }

    @Test
    void invokeSyncRejectsNonBlockingMode() {
        InvocationEngine engine = new InvocationEngine(List.of(), contexts);
        ServiceContext service = context().service();
        ServiceContext nonBlocking = new ServiceContext(
                service.requestId(),
                service.request(),
                service.security(),
                service.scope(),
                service.trace(),
                service.cancellation(),
                service.deadline(),
                service.attributes().with(InvocationEngine.EXECUTION_MODE, ExecutionMode.NON_BLOCKING));
        InvocationContext invocation = new InvocationContext(
                "inv-1", "op-1", nonBlocking, context().policy(), context().resource());

        assertThatThrownBy(() -> engine.invokeSync(invocation, () -> "nope"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }

    private static InvocationContext context() {
        ServiceContext service = new ServiceContext(
                "req-1",
                new RequestMetadata("GET", "/orders", "/orders", Map.of(), "127.0.0.1", null),
                ServicePrincipal.anonymous("local"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        OperationPolicy policy = new OperationPolicy(
                Set.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                AuditMode.BEST_EFFORT,
                ResponseProfile.LEGACY);
        return new InvocationContext(
                "inv-1",
                "op-1",
                service,
                policy,
                new ResourceRef("test", "1", ServiceScope.platform()));
    }

    private static final class RecordingInterceptor implements ServiceInterceptor {

        private final String interceptorId;
        private final int order;
        private final List<String> events;
        private final NexusException failure;

        private RecordingInterceptor(String interceptorId, int order, List<String> events, NexusException failure) {
            this.interceptorId = interceptorId;
            this.order = order;
            this.events = events;
            this.failure = failure;
        }

        @Override
        public String id() {
            return interceptorId;
        }

        @Override
        public int order() {
            return order;
        }

        @Override
        public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
            events.add("enter:" + interceptorId);
            if (failure != null) {
                return CompletableFuture.failedFuture(failure);
            }
            return CompletableFuture.completedFuture(outcome -> {
                events.add("finish:" + interceptorId);
                return CompletableFuture.completedFuture(null);
            });
        }
    }
}
