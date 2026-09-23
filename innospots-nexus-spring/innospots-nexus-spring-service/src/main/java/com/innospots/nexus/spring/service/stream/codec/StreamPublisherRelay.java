package com.innospots.nexus.spring.service.stream.codec;

import java.util.concurrent.Flow;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.stream.session.StreamSink;

/**
 * 将上游 {@link Flow.Publisher} 的事件依次写入对外的 {@link StreamSink}（例如 {@code StreamSession}）。
 */
public final class StreamPublisherRelay {

    private StreamPublisherRelay() {
    }

    /**
     * 订阅 {@code source}，按序 {@link StreamSink#emit} 到 {@code sink}；取消或失败时结束 sink。
     *
     * @param source    上游 Publisher（单订阅）
     * @param sink      对外 HTTP 会话 sink
     * @param eventType SSE/流事件 type，默认传 {@code "message"}
     * @param <T>       载荷类型
     */
    public static <T> void relay(Flow.Publisher<T> source, StreamSink<T> sink, String eventType) {
        Checks.notNull(source, "source");
        Checks.notNull(sink, "sink");
        Checks.notBlank(eventType, "eventType");
        source.subscribe(new RelaySubscriber<>(sink, eventType));
    }

    private static final class RelaySubscriber<T> implements Flow.Subscriber<T> {

        private final StreamSink<T> sink;
        private final String eventType;
        private Flow.Subscription subscription;

        private RelaySubscriber(StreamSink<T> sink, String eventType) {
            this.sink = sink;
            this.eventType = eventType;
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onNext(T item) {
            if (sink.isCancelled()) {
                subscription.cancel();
                return;
            }
            sink.emit(eventType, item).whenComplete((result, failure) -> {
                if (failure != null) {
                    subscription.cancel();
                    sink.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
                    return;
                }
                if (sink.isCancelled()) {
                    subscription.cancel();
                    return;
                }
                subscription.request(1);
            });
        }

        @Override
        public void onError(Throwable throwable) {
            if (throwable instanceof NexusException nexus) {
                sink.fail(nexus);
            } else {
                sink.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR));
            }
        }

        @Override
        public void onComplete() {
            sink.complete();
        }
    }
}
