package com.innospots.nexus.service.contract.audit;

/**
 * 参与主机当前事务的审计存储。
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditStorage
 * @see AuditEvent
 */
public interface TransactionalAuditStorage {

    /**
     * 在业务提交前于当前事务中追加 {@code event}。
     *
     * @param event 审计事件
     */
    void appendInCurrentTransaction(AuditEvent event);
}
