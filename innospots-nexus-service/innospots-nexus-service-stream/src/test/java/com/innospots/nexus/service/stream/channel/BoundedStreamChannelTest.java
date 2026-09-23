package com.innospots.nexus.service.stream.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.channel.OverflowPolicy;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.stream.config.StreamConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 有界通道背压、溢出与单订阅测试。
 */
class BoundedStreamChannelTest {

    @Test
    void rejectPolicyFailsWhenBufferFull() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.REJECT, 1, 128);
        channel.emit("first").toCompletableFuture().join();

        assertThatThrownBy(() -> channel.emit("second").toCompletableFuture().join())
                .hasCauseInstanceOf(NexusException.class)
                .cause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.BUFFER_OVERFLOW.fullCode());
    }

    @Test
    void dropLatestReturnsDroppedResult() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.DROP_LATEST, 1, 128);
        channel.emit("first").toCompletableFuture().join();

        EmitResult result = channel.emit("second").toCompletableFuture().join();

        assertThat(result).isEqualTo(EmitResult.DROPPED_LATEST);
    }

    @Test
    void dropOldestEvictsOldestItem() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.DROP_OLDEST, 1, 128);
        channel.emit("first").toCompletableFuture().join();

        EmitResult result = channel.emit("second").toCompletableFuture().join();

        assertThat(result).isEqualTo(EmitResult.DROPPED_OLDEST);
        assertThat(channel.bufferedItems()).isEqualTo(1);
    }

    @Test
    void closePolicyFailsEmit() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.CLOSE, 1, 128);
        channel.emit("first").toCompletableFuture().join();

        assertThatThrownBy(() -> channel.emit("second").toCompletableFuture().join())
                .hasCauseInstanceOf(NexusException.class)
                .cause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.BUFFER_OVERFLOW.fullCode());
    }

    @Test
    void nonPositiveRequestViolatesFlowProtocol() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.REJECT, 4, 512);
        AtomicReference<Throwable> error = new AtomicReference<>();
        channel.publisher().subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(0);
            }

            @Override
            public void onNext(String item) {
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onComplete() {
            }
        });

        assertThat(error.get()).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void secondSubscriptionIsRejected() {
        BoundedStreamChannel<String> channel = channel(OverflowPolicy.REJECT, 4, 512);
        List<Throwable> errors = new ArrayList<>();
        Flow.Subscriber<String> first = noopSubscriber();
        Flow.Subscriber<String> second = new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
            }

            @Override
            public void onNext(String item) {
            }

            @Override
            public void onError(Throwable throwable) {
                errors.add(throwable);
            }

            @Override
            public void onComplete() {
            }
        };
        channel.publisher().subscribe(first);
        channel.publisher().subscribe(second);

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst())
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(ServiceStatusCode.LIFECYCLE_CLOSED.fullCode());
    }

    private static BoundedStreamChannel<String> channel(OverflowPolicy overflow, int bufferSize, long bufferBytes) {
        StreamConfig config = new StreamConfig(
                10,
                bufferSize,
                bufferBytes,
                4096,
                1024,
                overflow,
                java.time.Duration.ofSeconds(30),
                java.time.Duration.ofMinutes(1),
                java.time.Duration.ofMinutes(1),
                java.time.Duration.ofSeconds(15),
                java.time.Duration.ofSeconds(2));
        return new BoundedStreamChannel<>(config, CancellationToken.none(), value -> 64L);
    }

    private static Flow.Subscriber<String> noopSubscriber() {
        return new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(1);
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
        };
    }
}
