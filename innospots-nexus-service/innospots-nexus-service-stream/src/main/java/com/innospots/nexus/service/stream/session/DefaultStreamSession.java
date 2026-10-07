package com.innospots.nexus.service.stream.session;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToLongFunction;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.stream.channel.BoundedStreamChannel;
import com.innospots.nexus.service.stream.channel.EmitResult;
import com.innospots.nexus.service.stream.channel.StreamChannel;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.event.StreamEvent;
import com.innospots.nexus.service.stream.event.StreamEventType;

/**
 * 默认 {@link StreamSession}，同时作为 {@link StreamSink} 暴露。
 *
 * @param <T> 载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public final class DefaultStreamSession<T> implements StreamSession<T> {

    private final String sessionId;
    private final String ownerId;
    private final ServiceScope scope;
    private final Class<T> eventClass;
    private final Instant createdAt;
    private final AtomicReference<Instant> lastActivityAt;
    private final AtomicReference<StreamState> state = new AtomicReference<>(StreamState.CREATED);
    private final BoundedStreamChannel<StreamEvent<T>> channel;
    private final CancellationToken token;
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicBoolean subscribed = new AtomicBoolean();
    private final Runnable registryEviction;
    private final AtomicBoolean registryEvicted = new AtomicBoolean();

    /**
     * 创建会话。
     *
     * @param sessionId         会话标识
     * @param ownerId           创建者标识
     * @param scope             作用域
     * @param eventClass        事件类型
     * @param config            配置
     * @param token             取消令牌
     * @param sizeEstimator     事件大小估算
     * @param registryEviction  终态后从 Manager 注册表摘除（可为 {@code null}）
     */
    public DefaultStreamSession(
            String sessionId,
            String ownerId,
            ServiceScope scope,
            Class<T> eventClass,
            StreamConfig config,
            CancellationToken token,
            ToLongFunction<StreamEvent<T>> sizeEstimator,
            Runnable registryEviction) {
        this.sessionId = Checks.notBlank(sessionId, "sessionId");
        this.ownerId = Checks.notBlank(ownerId, "ownerId");
        this.scope = Checks.notNull(scope, "scope");
        this.eventClass = Checks.notNull(eventClass, "eventClass");
        this.createdAt = Instant.now();
        this.lastActivityAt = new AtomicReference<>(createdAt);
        this.token = Checks.notNull(token, "token");
        this.channel = new BoundedStreamChannel<>(config, token, sizeEstimator);
        this.registryEviction = registryEviction;
    }

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public StreamState state() {
        return state.get();
    }

    @Override
    public boolean isCancelled() {
        return token.isCancelled();
    }

    @Override
    public Instant createdAt() {
        return createdAt;
    }

    @Override
    public Instant lastActivityAt() {
        return lastActivityAt.get();
    }

    @Override
    public StreamChannel<StreamEvent<T>> channel() {
        return channel;
    }

    @Override
    public Flow.Publisher<StreamEvent<T>> publisher() {
        return subscriber -> channel.publisher().subscribe(wrapSubscriber(subscriber));
    }

    @Override
    public CompletionStage<EmitResult> emit(T data) {
        return emit(StreamEventType.MESSAGE.wireName(), data);
    }

    @Override
    public CompletionStage<EmitResult> emit(String type, T data) {
        Checks.notBlank(type, "type");
        if (data == null) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.INVALID_PARAMETER));
        }
        long nextSequence = sequence.incrementAndGet();
        StreamEvent<T> event = new StreamEvent<>(
                sessionId + ":" + nextSequence,
                nextSequence,
                type,
                Instant.now(),
                data);
        if (!StreamEventType.HEARTBEAT.wireName().equals(type)) {
            lastActivityAt.set(event.timestamp());
        }
        return channel.emit(event);
    }

    @Override
    public CompletionStage<Void> complete() {
        return channel.complete().whenComplete((ignored, error) -> {
            state.set(StreamState.COMPLETED);
            evictFromRegistry();
        });
    }

    @Override
    public CompletionStage<Void> fail(NexusException failure) {
        state.set(StreamState.FAILED);
        return channel.fail(failure).whenComplete((ignored, error) -> evictFromRegistry());
    }

    @Override
    public CompletionStage<Void> cancel(CancellationReason reason) {
        state.set(StreamState.CANCELLED);
        return channel.cancel(reason).whenComplete((ignored, error) -> evictFromRegistry());
    }

    /**
     * 返回创建时作用域。
     *
     * @return 作用域
     */
    public ServiceScope ownerScope() {
        return scope;
    }

    /**
     * 返回当前快照。
     *
     * @return 快照
     */
    public StreamSnapshot snapshot() {
        return new StreamSnapshot(
                sessionId,
                ownerId,
                scope,
                state.get(),
                createdAt,
                lastActivityAt.get(),
                channel.bufferedItems(),
                channel.bufferedBytes());
    }

    /**
     * 返回创建者标识。
     *
     * @return 创建者
     */
    public String ownerId() {
        return ownerId;
    }

    /**
     * 返回捕获的事件类型。
     *
     * @return 事件类型
     */
    public Class<T> eventClass() {
        return eventClass;
    }

    /**
     * 若仍为 CREATED 则标记取消。
     */
    public void cancelIfUnsubscribed() {
        if (state.compareAndSet(StreamState.CREATED, StreamState.CANCELLED)) {
            channel.cancel(CancellationReason.APPLICATION_CANCELLED)
                    .whenComplete((ignored, error) -> evictFromRegistry());
        }
    }

    private void evictFromRegistry() {
        if (registryEviction == null) {
            return;
        }
        if (registryEvicted.compareAndSet(false, true)) {
            registryEviction.run();
        }
    }

    private Flow.Subscriber<StreamEvent<T>> wrapSubscriber(Flow.Subscriber<? super StreamEvent<T>> downstream) {
        return new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscribed.set(true);
                if (state.compareAndSet(StreamState.CREATED, StreamState.OPEN)) {
                    downstream.onSubscribe(subscription);
                    return;
                }
                subscription.cancel();
                downstream.onError(NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED));
            }

            @Override
            public void onNext(StreamEvent<T> item) {
                downstream.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                state.set(StreamState.FAILED);
                downstream.onError(throwable);
            }

            @Override
            public void onComplete() {
                state.set(StreamState.COMPLETED);
                downstream.onComplete();
            }
        };
    }
}
