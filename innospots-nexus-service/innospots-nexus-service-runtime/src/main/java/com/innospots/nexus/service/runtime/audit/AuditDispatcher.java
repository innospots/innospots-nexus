package com.innospots.nexus.service.runtime.audit;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.audit.AuditEvent;
import com.innospots.nexus.service.contract.audit.AuditStorage;
import com.innospots.nexus.service.contract.audit.TransactionalAuditStorage;
import com.innospots.nexus.service.contract.policy.AuditMode;

/**
 * 尽力而为审计事件分发器。队列满时丢弃并计数。
 *
 * @author Smars
 * @date 2026/09/15
 * @see AuditInterceptor
 * @see AuditLifecycle
 */
public final class AuditDispatcher {

    private final AuditStorage storage;
    private final Optional<TransactionalAuditStorage> transactionalStorage;
    private final int queueCapacity;
    private final AtomicInteger queued = new AtomicInteger();
    private final AtomicLong droppedCount = new AtomicLong();

    /**
     * 创建分发器。
     *
     * @param storage               异步审计存储
     * @param transactionalStorage  可选事务审计存储
     * @param queueCapacity         队列容量
     */
    public AuditDispatcher(
            AuditStorage storage,
            Optional<TransactionalAuditStorage> transactionalStorage,
            int queueCapacity) {
        this.storage = Checks.notNull(storage, "storage");
        this.transactionalStorage = Checks.notNull(transactionalStorage, "transactionalStorage");
        if (queueCapacity <= 0) {
            throw NexusException.build(com.innospots.nexus.base.status.NexusStatusCode.CONFIG_ERROR);
        }
        this.queueCapacity = queueCapacity;
    }

    /**
     * 返回已丢弃事件计数。
     *
     * @return 丢弃计数
     */
    public long droppedCount() {
        return droppedCount.get();
    }

    /**
     * 是否支持 REQUIRED 模式。
     *
     * @return 有事务审计存储时为 {@code true}
     */
    public boolean supportsRequired() {
        return transactionalStorage.isPresent();
    }

    /**
     * 在 {@code mode} 下追加 {@code event}。
     *
     * @param mode  审计模式
     * @param event 审计事件
     * @return 完成阶段
     */
    public CompletionStage<Void> dispatch(AuditMode mode, AuditEvent event) {
        Checks.notNull(mode, "mode");
        Checks.notNull(event, "event");
        if (mode == AuditMode.REQUIRED) {
            TransactionalAuditStorage transactional = transactionalStorage.orElse(null);
            if (transactional == null) {
                throw NexusException.build(com.innospots.nexus.service.contract.status.ServiceStatusCode.AUDIT_UNAVAILABLE);
            }
            transactional.appendInCurrentTransaction(event);
            return CompletableFuture.completedFuture(null);
        }
        if (queued.incrementAndGet() > queueCapacity) {
            queued.decrementAndGet();
            droppedCount.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }
        return storage.append(event).whenComplete((ignored, error) -> queued.decrementAndGet());
    }

    /**
     * 刷新缓冲事件。
     *
     * @param timeout 刷新超时
     * @return 完成阶段
     */
    public CompletionStage<Void> flush(Duration timeout) {
        Checks.notNull(timeout, "timeout");
        return storage.flush(timeout);
    }
}
