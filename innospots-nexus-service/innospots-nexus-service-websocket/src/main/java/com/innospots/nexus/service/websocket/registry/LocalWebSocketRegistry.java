package com.innospots.nexus.service.websocket.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.ConnectionState;
import com.innospots.nexus.service.websocket.session.WebSocketClose;

/**
 * 进程内 {@link WebSocketRegistry} 实现。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class LocalWebSocketRegistry implements WebSocketRegistry {

    private static final int LIST_BY_PRINCIPAL_LIMIT = 256;

    private final WebSocketRuntimeConfig config;
    private final ThreadBoundServiceContext contexts;
    private final Map<String, WebSocketConnectionBinding> connections = new ConcurrentHashMap<>();
    private final Map<SessionKey, Set<String>> sessionIndex = new ConcurrentHashMap<>();
    private final Map<PrincipalKey, Set<String>> principalIndex = new ConcurrentHashMap<>();

    private LocalWebSocketRegistry(Builder builder) {
        this.config = builder.config;
        this.contexts = builder.contexts;
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
    public void register(WebSocketConnectionBinding binding) {
        Checks.notNull(binding, "binding");
        synchronized (this) {
            if (connections.size() >= config.maxConnections()) {
                throw NexusException.build(ServiceStatusCode.CAPACITY_EXHAUSTED);
            }
            PrincipalKey principalKey = principalKey(binding);
            Set<String> principalConnections = principalIndex.computeIfAbsent(principalKey, ignored -> new HashSet<>());
            if (principalConnections.size() >= config.maxConnectionsPerUser()) {
                throw NexusException.build(ServiceStatusCode.CAPACITY_EXHAUSTED);
            }
            connections.put(binding.connectionId(), binding);
            sessionIndex.computeIfAbsent(sessionKey(binding), ignored -> new HashSet<>())
                    .add(binding.connectionId());
            principalConnections.add(binding.connectionId());
        }
    }

    @Override
    public void unregister(String connectionId) {
        Checks.notBlank(connectionId, "connectionId");
        WebSocketConnectionBinding removed = connections.remove(connectionId);
        if (removed == null) {
            return;
        }
        synchronized (this) {
            removeFromIndex(sessionIndex, sessionKey(removed), connectionId);
            removeFromIndex(principalIndex, principalKey(removed), connectionId);
        }
    }

    @Override
    public CompletionStage<Void> send(String connectionId, WebSocketMessage<?> message) {
        ServiceContext context = contexts.requireCurrent();
        WebSocketConnectionBinding binding = requireConnection(connectionId);
        authorizeScope(context, binding);
        validateOutputType(binding, message);
        return deliver(binding, message);
    }

    @Override
    public CompletionStage<BroadcastResult> sendToSession(String sessionId, WebSocketMessage<?> message) {
        Checks.notBlank(sessionId, "sessionId");
        Checks.notNull(message, "message");
        ServiceContext context = contexts.requireCurrent();
        List<WebSocketConnectionBinding> targets = snapshotSessionTargets(context, sessionId);
        int attempted = targets.size();
        int sent = 0;
        int rejected = 0;
        int unavailable = 0;
        for (WebSocketConnectionBinding binding : targets) {
            SendOutcome outcome = sendToBinding(context, binding, message);
            switch (outcome) {
                case SENT -> sent++;
                case REJECTED -> rejected++;
                case UNAVAILABLE -> unavailable++;
                default -> rejected++;
            }
        }
        return CompletableFuture.completedFuture(new BroadcastResult(attempted, sent, rejected, unavailable));
    }

    @Override
    public CompletionStage<Void> close(String connectionId, WebSocketClose close) {
        Checks.notBlank(connectionId, "connectionId");
        Checks.notNull(close, "close");
        ServiceContext context = contexts.requireCurrent();
        WebSocketConnectionBinding binding = connections.get(connectionId);
        if (binding == null) {
            return CompletableFuture.completedFuture(null);
        }
        authorizeOwner(context, binding);
        return binding.close(close).whenComplete((ignored, error) -> unregister(connectionId));
    }

    @Override
    public List<ConnectionSnapshot> listByPrincipal(ServicePrincipal principal, ServiceScope scope) {
        Checks.notNull(principal, "principal");
        Checks.notNull(scope, "scope");
        PrincipalKey key = new PrincipalKey(principal.realm(), scope, principal.type(), principal.id());
        Set<String> connectionIds = principalIndex.getOrDefault(key, Set.of());
        List<ConnectionSnapshot> snapshots = new ArrayList<>(Math.min(connectionIds.size(), LIST_BY_PRINCIPAL_LIMIT));
        for (String connectionId : connectionIds) {
            if (snapshots.size() >= LIST_BY_PRINCIPAL_LIMIT) {
                break;
            }
            WebSocketConnectionBinding binding = connections.get(connectionId);
            if (binding == null) {
                continue;
            }
            snapshots.add(toSnapshot(binding));
        }
        return Collections.unmodifiableList(snapshots);
    }

    /**
     * 返回当前注册连接数。
     *
     * @return 连接数
     */
    public int activeConnections() {
        return connections.size();
    }

    private SendOutcome sendToBinding(
            ServiceContext context,
            WebSocketConnectionBinding binding,
            WebSocketMessage<?> message) {
        if (binding.state() != ConnectionState.OPEN) {
            return SendOutcome.UNAVAILABLE;
        }
        try {
            authorizeScope(context, binding);
            validateOutputType(binding, message);
            deliver(binding, message).toCompletableFuture().join();
            return SendOutcome.SENT;
        } catch (NexusException failure) {
            if (NexusStatusCode.NO_PERMISSION.fullCode().equals(failure.code())
                    || ServiceStatusCode.MESSAGE_TYPE_UNKNOWN.fullCode().equals(failure.code())) {
                return SendOutcome.REJECTED;
            }
            if (ServiceStatusCode.LIFECYCLE_CLOSED.fullCode().equals(failure.code())) {
                return SendOutcome.UNAVAILABLE;
            }
            return SendOutcome.REJECTED;
        } catch (RuntimeException failure) {
            if (failure.getCause() instanceof NexusException nexusException) {
                return sendFailureOutcome(nexusException);
            }
            return SendOutcome.UNAVAILABLE;
        }
    }

    private static SendOutcome sendFailureOutcome(NexusException failure) {
        if (NexusStatusCode.NO_PERMISSION.fullCode().equals(failure.code())
                || ServiceStatusCode.MESSAGE_TYPE_UNKNOWN.fullCode().equals(failure.code())) {
            return SendOutcome.REJECTED;
        }
        if (ServiceStatusCode.LIFECYCLE_CLOSED.fullCode().equals(failure.code())) {
            return SendOutcome.UNAVAILABLE;
        }
        return SendOutcome.UNAVAILABLE;
    }

    private CompletionStage<Void> deliver(WebSocketConnectionBinding binding, WebSocketMessage<?> message) {
        if (binding.state() != ConnectionState.OPEN) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.LIFECYCLE_CLOSED));
        }
        return binding.deliver(message);
    }

    private WebSocketConnectionBinding requireConnection(String connectionId) {
        Checks.notBlank(connectionId, "connectionId");
        WebSocketConnectionBinding binding = connections.get(connectionId);
        if (binding == null) {
            throw NexusException.build(NexusStatusCode.RESOURCE_NOT_FOUND);
        }
        return binding;
    }

    private List<WebSocketConnectionBinding> snapshotSessionTargets(ServiceContext context, String sessionId) {
        SessionKey key = new SessionKey(context.security().realm(), context.scope(), sessionId);
        Set<String> connectionIds;
        synchronized (this) {
            connectionIds = Set.copyOf(sessionIndex.getOrDefault(key, Set.of()));
        }
        List<WebSocketConnectionBinding> targets = new ArrayList<>(connectionIds.size());
        for (String connectionId : connectionIds) {
            WebSocketConnectionBinding binding = connections.get(connectionId);
            if (binding != null) {
                targets.add(binding);
            }
        }
        return targets;
    }

    private static void validateOutputType(WebSocketConnectionBinding binding, WebSocketMessage<?> message) {
        Checks.notNull(message, "message");
        if (!binding.allowedOutputTypes().contains(message.type())) {
            throw NexusException.build(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN);
        }
    }

    private static void authorizeScope(ServiceContext context, WebSocketConnectionBinding binding) {
        if (!sameScope(context.scope(), binding.scope())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        if (!context.security().realm().equals(binding.realm())) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private static void authorizeOwner(ServiceContext context, WebSocketConnectionBinding binding) {
        authorizeScope(context, binding);
        if (!context.security().id().equals(binding.principal().id())
                || context.security().type() != binding.principal().type()) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private static ConnectionSnapshot toSnapshot(WebSocketConnectionBinding binding) {
        return new ConnectionSnapshot(
                binding.connectionId(),
                binding.sessionId(),
                binding.realm(),
                binding.scope(),
                binding.principal(),
                binding.state());
    }

    private static SessionKey sessionKey(WebSocketConnectionBinding binding) {
        return new SessionKey(binding.realm(), binding.scope(), binding.sessionId());
    }

    private static PrincipalKey principalKey(WebSocketConnectionBinding binding) {
        return new PrincipalKey(
                binding.realm(),
                binding.scope(),
                binding.principal().type(),
                binding.principal().id());
    }

    private static void removeFromIndex(
            Map<?, Set<String>> index,
            Object key,
            String connectionId) {
        Set<String> connectionIds = index.get(key);
        if (connectionIds == null) {
            return;
        }
        connectionIds.remove(connectionId);
        if (connectionIds.isEmpty()) {
            index.remove(key);
        }
    }

    private static boolean sameScope(ServiceScope left, ServiceScope right) {
        return nullSafe(left.tenantId()).equals(nullSafe(right.tenantId()))
                && nullSafe(left.workspaceId()).equals(nullSafe(right.workspaceId()))
                && nullSafe(left.projectId()).equals(nullSafe(right.projectId()));
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private enum SendOutcome {
        SENT,
        REJECTED,
        UNAVAILABLE
    }

    private record SessionKey(String realm, ServiceScope scope, String sessionId) {
    }

    private record PrincipalKey(String realm, ServiceScope scope, PrincipalType type, String principalId) {
    }

    /**
     * 构建器。
     */
    public static final class Builder {

        private WebSocketRuntimeConfig config = WebSocketRuntimeConfig.defaults();
        private ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();

        private Builder() {
        }

        public Builder config(WebSocketRuntimeConfig config) {
            this.config = Checks.notNull(config, "config");
            return this;
        }

        public Builder contexts(ThreadBoundServiceContext contexts) {
            this.contexts = Checks.notNull(contexts, "contexts");
            return this;
        }

        public LocalWebSocketRegistry build() {
            return new LocalWebSocketRegistry(this);
        }
    }
}
