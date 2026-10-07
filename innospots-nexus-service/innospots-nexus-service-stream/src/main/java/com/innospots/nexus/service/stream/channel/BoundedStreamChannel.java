package com.innospots.nexus.service.stream.channel;

import java.util.ArrayDeque;
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
import com.innospots.nexus.service.contract.channel.OverflowPolicy;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.stream.config.StreamConfig;

/**
 * 有限队列、单订阅与背压的有界通道实现。
 *
 * @param <T> 元素类型
 * @author Smars
 * @date 2026/09/15
 */
public final class BoundedStreamChannel<T> implements StreamChannel<T> {

    private final StreamConfig config;
    private final CancellationToken token;
    private final ToLongFunction<T> sizeEstimator;
    private final ArrayDeque<T> queue = new ArrayDeque<>();
    private final AtomicLong demand = new AtomicLong();
    private final AtomicLong bufferedBytes = new AtomicLong();
    private final AtomicBoolean subscribed = new AtomicBoolean();
    private final AtomicBoolean terminal = new AtomicBoolean();
    private final AtomicReference<Flow.Subscriber<? super T>> subscriber = new AtomicReference<>();
    private final AtomicReference<Flow.Subscription> subscription = new AtomicReference<>();
    private final AtomicBoolean draining = new AtomicBoolean();
    private final CompletableFuture<Void> closed = new CompletableFuture<>();

    /**
     * 创建通道。
     *
     * @param config         配置
     * @param token          取消令牌
     * @param sizeEstimator  大小估算
     */
    public BoundedStreamChannel(
            StreamConfig config,
            CancellationToken token,
            ToLongFunction<T> sizeEstimator) {
        this.config = Checks.notNull(config, "config");
        this.token = Checks.notNull(token, "token");
        this.sizeEstimator = Checks.notNull(sizeEstimator, "sizeEstimator");
    }

    @Override
    public CompletionStage<EmitResult> emit(T value) {
        Checks.notNull(value, "value");
        if (token.isCancelled() || terminal.get()) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED));
        }
        long size = sizeOf(value);
        if (size > config.maxEventBytes()) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.INVALID_PARAMETER));
        }
        synchronized (queue) {
            if (terminal.get()) {
                return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED));
            }
            if (wouldOverflow(size)) {
                return handleOverflow(value, size);
            }
            queue.addLast(value);
            bufferedBytes.addAndGet(size);
        }
        drainLater();
        return CompletableFuture.completedFuture(EmitResult.ACCEPTED);
    }

    @Override
    public Flow.Publisher<T> publisher() {
        return this::subscribeOnce;
    }

    @Override
    public CompletionStage<Void> complete() {
        terminal.compareAndSet(false, true);
        drainLater();
        return closed;
    }

    @Override
    public CompletionStage<Void> fail(NexusException failure) {
        Checks.notNull(failure, "failure");
        terminal.compareAndSet(false, true);
        synchronized (queue) {
            queue.clear();
            bufferedBytes.set(0);
        }
        Flow.Subscriber<? super T> current = subscriber.get();
        if (current != null) {
            current.onError(failure);
        }
        closed.complete(null);
        return closed;
    }

    @Override
    public CompletionStage<Void> cancel(CancellationReason reason) {
        Checks.notNull(reason, "reason");
        terminal.compareAndSet(false, true);
        synchronized (queue) {
            queue.clear();
            bufferedBytes.set(0);
        }
        Flow.Subscriber<? super T> current = subscriber.get();
        if (current != null) {
            current.onError(NexusException.build(ServiceStatusCode.OPERATION_CANCELLED));
        }
        closed.complete(null);
        return closed;
    }

    /**
     * 返回当前缓冲项数。
     *
     * @return 项数
     */
    public int bufferedItems() {
        synchronized (queue) {
            return queue.size();
        }
    }

    /**
     * 返回当前缓冲字节。
     *
     * @return 字节
     */
    public long bufferedBytes() {
        return bufferedBytes.get();
    }

    private CompletionStage<EmitResult> handleOverflow(T value, long size) {
        OverflowPolicy policy = config.overflow();
        if (policy == OverflowPolicy.REJECT) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.BUFFER_OVERFLOW));
        }
        if (policy == OverflowPolicy.DROP_LATEST) {
            return CompletableFuture.completedFuture(EmitResult.DROPPED_LATEST);
        }
        if (policy == OverflowPolicy.DROP_OLDEST) {
            if (queue.isEmpty()) {
                return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.BUFFER_OVERFLOW));
            }
            T dropped = queue.removeFirst();
            bufferedBytes.addAndGet(-sizeOf(dropped));
            queue.addLast(value);
            bufferedBytes.addAndGet(size);
            drainLater();
            return CompletableFuture.completedFuture(EmitResult.DROPPED_OLDEST);
        }
        terminal.set(true);
        synchronized (queue) {
            queue.clear();
            bufferedBytes.set(0);
        }
        return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.BUFFER_OVERFLOW));
    }

    private boolean wouldOverflow(long size) {
        return queue.size() >= config.bufferSize()
                || bufferedBytes.get() + size > config.bufferBytes();
    }

    private long sizeOf(T value) {
        long size = sizeEstimator.applyAsLong(value);
        if (size < 0) {
            return 0;
        }
        return size;
    }

    private void subscribeOnce(Flow.Subscriber<? super T> downstream) {
        Checks.notNull(downstream, "downstream");
        if (!subscribed.compareAndSet(false, true)) {
            downstream.onSubscribe(new Flow.Subscription() {
                @Override
                public void request(long n) {
                }

                @Override
                public void cancel() {
                }
            });
            downstream.onError(NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED));
            return;
        }
        subscriber.set(downstream);
        downstream.onSubscribe(new Flow.Subscription() {
            @Override
            public void request(long n) {
                if (n <= 0L) {
                    downstream.onError(new IllegalArgumentException("non-positive request"));
                    return;
                }
                long updated = demand.updateAndGet(current -> {
                    long next = current + n;
                    if (next < 0L) {
                        return Long.MAX_VALUE;
                    }
                    return next;
                });
                if (updated < 0L) {
                    demand.set(Long.MAX_VALUE);
                }
                drainLater();
            }

            @Override
            public void cancel() {
                subscription.set(null);
            }
        });
        subscription.set(new Flow.Subscription() {
            @Override
            public void request(long n) {
            }

            @Override
            public void cancel() {
            }
        });
        drainLater();
    }

    private void drainLater() {
        if (!draining.compareAndSet(false, true)) {
            return;
        }
        try {
            drainLoop();
        } finally {
            draining.set(false);
        }
    }

    private void drainLoop() {
        while (true) {
            Flow.Subscriber<? super T> current = subscriber.get();
            if (current == null) {
                return;
            }
            T next;
            synchronized (queue) {
                if (queue.isEmpty()) {
                    if (terminal.get()) {
                        current.onComplete();
                        closed.complete(null);
                    }
                    return;
                }
                if (demand.get() <= 0L) {
                    return;
                }
                next = queue.removeFirst();
                bufferedBytes.addAndGet(-sizeOf(next));
            }
            demand.decrementAndGet();
            current.onNext(next);
        }
    }
}
