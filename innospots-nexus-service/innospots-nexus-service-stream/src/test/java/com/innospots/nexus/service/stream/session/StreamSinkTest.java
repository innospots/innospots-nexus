package com.innospots.nexus.service.stream.session;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.stream.channel.EmitResult;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.event.StreamEvent;
import com.innospots.nexus.service.stream.event.StreamEventType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StreamSink open/emit/cancel 行为测试。
 */
class StreamSinkTest {

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();

    @Test
    void openReturnsSinkWithDefaultMessageType() {
        DefaultStreamManager manager = manager();
        installContext(CancellationToken.none());
        StreamSink<String> sink = manager.open(String.class);

        assertThat(sink.sessionId()).isNotBlank();
        assertThat(sink.state()).isEqualTo(StreamState.CREATED);
        assertThat(sink.isCancelled()).isFalse();

        EmitResult result = sink.emit("hello").toCompletableFuture().join();
        assertThat(result).isEqualTo(EmitResult.ACCEPTED);

        DefaultStreamSession<String> session = (DefaultStreamSession<String>) sink;
        CompletableFuture<StreamEvent<String>> item = new CompletableFuture<>();
        session.publisher().subscribe(new java.util.concurrent.Flow.Subscriber<>() {
            @Override
            public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(StreamEvent<String> event) {
                item.complete(event);
            }

            @Override
            public void onError(Throwable throwable) {
                item.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
            }
        });

        assertThat(item.join().type()).isEqualTo(StreamEventType.MESSAGE.wireName());
    }

    @Test
    void isCancelledReflectsToken() {
        CancellationSource source = new CancellationSource();
        installContext(source.token());
        DefaultStreamManager manager = manager();
        StreamSink<String> sink = manager.open(String.class);

        assertThat(sink.isCancelled()).isFalse();
        source.cancel(com.innospots.nexus.service.contract.cancellation.CancellationReason.CLIENT_DISCONNECTED);
        assertThat(sink.isCancelled()).isTrue();
    }

    private DefaultStreamManager manager() {
        StreamConfig config = new StreamConfig(
                4,
                8,
                4096,
                8192,
                1024,
                com.innospots.nexus.service.contract.channel.OverflowPolicy.REJECT,
                java.time.Duration.ofSeconds(30),
                java.time.Duration.ofMinutes(1),
                java.time.Duration.ofMinutes(1),
                java.time.Duration.ofSeconds(15),
                java.time.Duration.ofSeconds(2));
        return DefaultStreamManager.builder().config(config).contexts(contexts).build();
    }

    private void installContext(CancellationToken token) {
        ServiceContext context = new ServiceContext(
                "req-stream",
                new RequestMetadata("GET", "/stream", "/stream", Map.of(), "127.0.0.1", null),
                new ServicePrincipal("user-1", PrincipalType.USER, "local", null, null, null),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                token,
                Deadline.unlimited(),
                ContextAttributes.empty());
        contexts.install(context);
    }
}
