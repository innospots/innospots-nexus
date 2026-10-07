package com.innospots.nexus.service.stream.session;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.stream.channel.EmitResult;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.event.StreamEvent;

/**
 * 默认 {@link StreamManager} 实现。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class DefaultStreamManager implements StreamManager {

    private final StreamConfig config;
    private final ThreadBoundServiceContext contexts;
    private final ScheduledExecutorService scheduler;
    private final Cache<String, DefaultStreamSession<?>> sessions;
    private final ToLongFunction<StreamEvent<?>> defaultSizeEstimator = event -> 64L;

    private DefaultStreamManager(Builder builder) {
        this.config = builder.config;
        this.contexts = builder.contexts;
        this.scheduler = builder.scheduler;
        this.sessions = buildSessionCache(config);
    }

    /**
     * 返回构建器。
     *
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public <T> StreamSink<T> open(Class<T> eventType) {
        Checks.notNull(eventType, "eventType");
        return openInternal(eventType);
    }

    @Override
    public <T> StreamSink<T> open(Type eventType) {
        Checks.notNull(eventType, "eventType");
        if (eventType instanceof Class<?> clazz) {
            return openInternal(castClass(clazz));
        }
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
    }

    @Override
    public Optional<StreamSnapshot> find(String sessionId) {
        Checks.notBlank(sessionId, "sessionId");
        DefaultStreamSession<?> session = sessions.getIfPresent(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        return Optional.of(session.snapshot());
    }

    @Override
    public <T> CompletionStage<EmitResult> emit(String sessionId, String type, T data) {
        DefaultStreamSession<T> session = requireAuthorizedSession(sessionId);
        if (data == null) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.INVALID_PARAMETER));
        }
        if (!session.eventClass().isInstance(data)) {
            return CompletableFuture.failedFuture(NexusException.build(NexusStatusCode.INVALID_PARAMETER));
        }
        return session.emit(type, data);
    }

    @Override
    public CompletionStage<Void> fail(String sessionId, NexusException failure) {
        DefaultStreamSession<?> session = requireAuthorizedSession(sessionId);
        return session.fail(failure);
    }

    @Override
    public CompletionStage<Void> cancel(String sessionId, CancellationReason reason) {
        DefaultStreamSession<?> session = requireAuthorizedSession(sessionId);
        return session.cancel(reason);
    }

    @Override
    public CompletionStage<Void> close() {
        CompletableFuture<Void> closed = new CompletableFuture<>();
        List<DefaultStreamSession<?>> active = new ArrayList<>(sessions.asMap().values());
        sessions.invalidateAll();
        CompletableFuture<?>[] pending = active.stream()
                .map(session -> session.cancel(CancellationReason.SERVER_SHUTDOWN))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(pending).whenComplete((ignored, error) -> closed.complete(null));
        return closed;
    }

    /**
     * 返回当前注册表中的会话数。
     *
     * @return 会话数
     */
    public int activeSessions() {
        return (int) sessions.estimatedSize();
    }

    private <T> StreamSink<T> openInternal(Class<T> eventType) {
        ServiceContext context = contexts.requireCurrent();
        if (sessions.estimatedSize() >= config.maxSessions()) {
            throw NexusException.build(ServiceStatusCode.CAPACITY_EXHAUSTED);
        }
        String sessionId = UUID.randomUUID().toString();
        Runnable evictFromRegistry = () -> sessions.invalidate(sessionId);
        DefaultStreamSession<T> session = new DefaultStreamSession<>(
                sessionId,
                context.security().id(),
                context.scope(),
                eventType,
                config,
                context.cancellation(),
                event -> defaultSizeEstimator.applyAsLong(event),
                evictFromRegistry);
        sessions.put(sessionId, session);
        scheduleSubscribeTimeout(session);
        return session;
    }

    @SuppressWarnings("unchecked")
    private <T> DefaultStreamSession<T> requireAuthorizedSession(String sessionId) {
        Checks.notBlank(sessionId, "sessionId");
        ServiceContext context = contexts.requireCurrent();
        DefaultStreamSession<?> session = sessions.getIfPresent(sessionId);
        if (session == null) {
            throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
        }
        if (!session.ownerId().equals(context.security().id())
                || !sameScope(session.ownerScope(), context.scope())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        return (DefaultStreamSession<T>) session;
    }

    private void scheduleSubscribeTimeout(DefaultStreamSession<?> session) {
        if (scheduler == null) {
            return;
        }
        scheduler.schedule(
                session::cancelIfUnsubscribed,
                config.subscribeTimeout().toMillis(),
                TimeUnit.MILLISECONDS);
    }

    private static Cache<String, DefaultStreamSession<?>> buildSessionCache(StreamConfig config) {
        return Caffeine.newBuilder()
                .maximumSize(config.maxSessions())
                .expireAfterAccess(config.idleTimeout())
                .expireAfterWrite(config.ttl())
                .removalListener((String sessionId, DefaultStreamSession<?> session, RemovalCause cause) -> {
                    if (session == null) {
                        return;
                    }
                    if (cause == RemovalCause.EXPLICIT || cause == RemovalCause.REPLACED) {
                        return;
                    }
                    StreamState state = session.state();
                    if (state == StreamState.COMPLETED
                            || state == StreamState.FAILED
                            || state == StreamState.CANCELLED) {
                        return;
                    }
                    session.cancel(CancellationReason.SERVER_SHUTDOWN);
                })
                .build();
    }

    private static boolean sameScope(ServiceScope left, ServiceScope right) {
        return nullSafe(left.tenantId()).equals(nullSafe(right.tenantId()))
                && nullSafe(left.workspaceId()).equals(nullSafe(right.workspaceId()))
                && nullSafe(left.projectId()).equals(nullSafe(right.projectId()));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> castClass(Class<?> type) {
        return (Class<T>) type;
    }

    /**
     * 构建器。
     */
    public static final class Builder {

        private StreamConfig config = StreamConfig.defaults();
        private ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
        private ScheduledExecutorService scheduler;

        private Builder() {
        }

        public Builder config(StreamConfig config) {
            this.config = Checks.notNull(config, "config");
            return this;
        }

        public Builder contexts(ThreadBoundServiceContext contexts) {
            this.contexts = Checks.notNull(contexts, "contexts");
            return this;
        }

        public Builder scheduler(ScheduledExecutorService scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        public DefaultStreamManager build() {
            return new DefaultStreamManager(this);
        }
    }
}
