package com.innospots.nexus.service.runtime.audit;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.audit.AuditStorage;
import com.innospots.nexus.service.contract.audit.TransactionalAuditStorage;

/**
 * 审计运行时生命周期：启动校验与关闭刷新。
 *
 * @author Smars
 * @date 2026/09/15
 * @see AuditDispatcher
 */
public final class AuditLifecycle {

    private final AuditDispatcher dispatcher;
    private final boolean auditEnabled;

    /**
     * 创建生命周期协调器。
     *
     * @param storage               异步审计存储
     * @param transactionalStorage  可选事务审计存储
     * @param queueCapacity         队列容量
     * @param auditEnabled          是否启用审计
     */
    public AuditLifecycle(
            AuditStorage storage,
            Optional<TransactionalAuditStorage> transactionalStorage,
            int queueCapacity,
            boolean auditEnabled) {
        this.dispatcher = new AuditDispatcher(storage, transactionalStorage, queueCapacity);
        this.auditEnabled = auditEnabled;
    }

    /**
     * 返回关联分发器。
     *
     * @return 分发器
     */
    public AuditDispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * 返回审计拦截器。
     *
     * @return 拦截器
     */
    public AuditInterceptor interceptor() {
        return new AuditInterceptor(dispatcher, auditEnabled);
    }

    /**
     * 启动时校验。一期未启用时不做 REQUIRED 校验。
     */
    public void start() {
    }

    /**
     * 关闭前刷新缓冲事件。
     *
     * @param timeout 刷新超时
     * @return 完成阶段
     */
    public CompletionStage<Void> stop(Duration timeout) {
        Checks.notNull(timeout, "timeout");
        if (!auditEnabled) {
            return dispatcher.flush(timeout);
        }
        return dispatcher.flush(timeout);
    }
}
