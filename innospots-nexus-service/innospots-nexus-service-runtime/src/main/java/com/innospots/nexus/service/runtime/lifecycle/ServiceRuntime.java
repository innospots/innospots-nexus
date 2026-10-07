package com.innospots.nexus.service.runtime.lifecycle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.audit.AuditStorage;
import com.innospots.nexus.service.contract.audit.TransactionalAuditStorage;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.runtime.audit.AuditLifecycle;
import com.innospots.nexus.service.runtime.context.ContextPropagation;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.idempotency.IdempotencyCoordinator;
import com.innospots.nexus.service.runtime.invocation.ControlInterceptor;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.security.AuthenticationCoordinator;
import com.innospots.nexus.service.runtime.security.AuthorizationInterceptor;
import com.innospots.nexus.service.runtime.time.DeadlineInterceptor;

/**
 * 中立服务运行时：组装拦截器链、上下文与启停生命周期。
 *
 * @author Smars
 * @date 2026/09/15
 * @see RuntimeState
 */
public final class ServiceRuntime {

    private final ThreadBoundServiceContext contexts;
    private final ContextPropagation propagation;
    private final InvocationEngine engine;
    private final AuditLifecycle auditLifecycle;
    private final AtomicReference<RuntimeState> state = new AtomicReference<>(RuntimeState.CREATED);

    private ServiceRuntime(Builder builder) {
        this.contexts = builder.contexts;
        this.propagation = new ContextPropagation(contexts);
        this.auditLifecycle = new AuditLifecycle(
                builder.auditStorage,
                builder.transactionalAuditStorage,
                builder.auditQueueCapacity,
                builder.auditEnabled);
        this.engine = new InvocationEngine(buildInterceptors(builder, auditLifecycle), contexts);
    }

    /**
     * 返回新建构建器。
     *
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 返回当前状态。
     *
     * @return 状态
     */
    public RuntimeState state() {
        return state.get();
    }

    /**
     * 是否允许新调用准入。
     *
     * @return 可准入时为 {@code true}
     */
    public boolean admitsNewWork() {
        RuntimeState current = state.get();
        return current == RuntimeState.READY;
    }

    /**
     * 返回调用引擎。
     *
     * @return 引擎
     */
    public InvocationEngine engine() {
        return engine;
    }

    /**
     * 返回上下文传播器。
     *
     * @return 传播器
     */
    public ContextPropagation propagation() {
        return propagation;
    }

    /**
     * 返回线程绑定上下文访问器。
     *
     * @return 访问器
     */
    public ThreadBoundServiceContext contexts() {
        return contexts;
    }

    /**
     * 启动运行时。幂等：已 READY 时直接返回。
     */
    public synchronized void start() {
        RuntimeState current = state.get();
        if (current == RuntimeState.READY) {
            return;
        }
        if (current == RuntimeState.CLOSED || current == RuntimeState.FAILED) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        state.set(RuntimeState.STARTING);
        try {
            auditLifecycle.start();
            state.set(RuntimeState.READY);
        } catch (RuntimeException ex) {
            state.set(RuntimeState.FAILED);
            throw ex;
        }
    }

    /**
     * 停止新准入并刷新审计。幂等：已 STOPPED/CLOSED 时直接返回。
     *
     * @param grace 关闭宽限期
     * @return 完成阶段
     */
    public synchronized CompletionStage<Void> stop(Duration grace) {
        Checks.notNull(grace, "grace");
        RuntimeState current = state.get();
        if (current == RuntimeState.STOPPED || current == RuntimeState.CLOSED) {
            return CompletableFuture.completedFuture(null);
        }
        if (current != RuntimeState.READY) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        state.set(RuntimeState.STOPPING);
        return auditLifecycle.stop(grace).whenComplete((ignored, error) -> state.set(RuntimeState.STOPPED));
    }

    /**
     * 关闭运行时。幂等：已 CLOSED 时直接返回。
     *
     * @return 完成阶段
     */
    public synchronized CompletionStage<Void> close() {
        RuntimeState current = state.get();
        if (current == RuntimeState.CLOSED) {
            return CompletableFuture.completedFuture(null);
        }
        if (current == RuntimeState.READY) {
            return stop(Duration.ofSeconds(30)).thenCompose(ignored -> closeFromStopped());
        }
        if (current == RuntimeState.STOPPED) {
            return closeFromStopped();
        }
        state.set(RuntimeState.CLOSED);
        return CompletableFuture.completedFuture(null);
    }

    private CompletionStage<Void> closeFromStopped() {
        state.set(RuntimeState.CLOSED);
        return CompletableFuture.completedFuture(null);
    }

    private static List<ServiceInterceptor> buildInterceptors(Builder builder, AuditLifecycle auditLifecycle) {
        List<ServiceInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new ControlInterceptor());
        interceptors.add(new DeadlineInterceptor());
        if (builder.securityProvider != null) {
            interceptors.add(new AuthenticationCoordinator(builder.securityProvider, builder.contexts));
        }
        if (builder.permissionProvider != null) {
            interceptors.add(new AuthorizationInterceptor(builder.permissionProvider, builder.contexts));
        }
        interceptors.add(auditLifecycle.interceptor());
        interceptors.add(new IdempotencyCoordinator(builder.idempotencyEnabled));
        interceptors.addAll(builder.extraInterceptors);
        return interceptors;
    }

    /**
     * {@link ServiceRuntime} 构建器。
     */
    public static final class Builder {

        private ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
        private SecurityProvider securityProvider;
        private PermissionProvider permissionProvider;
        private AuditStorage auditStorage = noopAuditStorage();
        private Optional<TransactionalAuditStorage> transactionalAuditStorage = Optional.empty();
        private int auditQueueCapacity = 4096;
        private boolean auditEnabled;
        private boolean idempotencyEnabled;
        private final List<ServiceInterceptor> extraInterceptors = new ArrayList<>();

        private Builder() {
        }

        /**
         * 设置线程绑定上下文访问器。
         *
         * @param contexts 访问器
         * @return 构建器
         */
        public Builder contexts(ThreadBoundServiceContext contexts) {
            this.contexts = Checks.notNull(contexts, "contexts");
            return this;
        }

        /**
         * 设置安全提供者。
         *
         * @param securityProvider 提供者
         * @return 构建器
         */
        public Builder securityProvider(SecurityProvider securityProvider) {
            this.securityProvider = securityProvider;
            return this;
        }

        /**
         * 设置权限提供者。
         *
         * @param permissionProvider 提供者
         * @return 构建器
         */
        public Builder permissionProvider(PermissionProvider permissionProvider) {
            this.permissionProvider = permissionProvider;
            return this;
        }

        /**
         * 设置审计存储。
         *
         * @param auditStorage 存储
         * @return 构建器
         */
        public Builder auditStorage(AuditStorage auditStorage) {
            this.auditStorage = Checks.notNull(auditStorage, "auditStorage");
            return this;
        }

        /**
         * 设置事务审计存储。
         *
         * @param transactionalAuditStorage 存储
         * @return 构建器
         */
        public Builder transactionalAuditStorage(TransactionalAuditStorage transactionalAuditStorage) {
            this.transactionalAuditStorage = Optional.of(
                    Checks.notNull(transactionalAuditStorage, "transactionalAuditStorage"));
            return this;
        }

        /**
         * 设置审计队列容量。
         *
         * @param auditQueueCapacity 容量
         * @return 构建器
         */
        public Builder auditQueueCapacity(int auditQueueCapacity) {
            this.auditQueueCapacity = auditQueueCapacity;
            return this;
        }

        /**
         * 启用审计。
         *
         * @param auditEnabled 是否启用
         * @return 构建器
         */
        public Builder auditEnabled(boolean auditEnabled) {
            this.auditEnabled = auditEnabled;
            return this;
        }

        /**
         * 启用幂等。
         *
         * @param idempotencyEnabled 是否启用
         * @return 构建器
         */
        public Builder idempotencyEnabled(boolean idempotencyEnabled) {
            this.idempotencyEnabled = idempotencyEnabled;
            return this;
        }

        /**
         * 追加额外拦截器。
         *
         * @param interceptor 拦截器
         * @return 构建器
         */
        public Builder addInterceptor(ServiceInterceptor interceptor) {
            extraInterceptors.add(Checks.notNull(interceptor, "interceptor"));
            return this;
        }

        /**
         * 构建运行时。
         *
         * @return 运行时
         */
        public ServiceRuntime build() {
            return new ServiceRuntime(this);
        }

        private static AuditStorage noopAuditStorage() {
            return new AuditStorage() {
                @Override
                public CompletionStage<Void> append(com.innospots.nexus.service.contract.audit.AuditEvent event) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletionStage<Void> flush(Duration timeout) {
                    return CompletableFuture.completedFuture(null);
                }
            };
        }
    }
}
