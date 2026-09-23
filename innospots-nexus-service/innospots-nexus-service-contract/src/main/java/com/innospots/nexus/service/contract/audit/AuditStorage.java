package com.innospots.nexus.service.contract.audit;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * 仅追加的审计存储。完成表示实现方已确认持久化。
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuditEvent
 * @see TransactionalAuditStorage
 */
public interface AuditStorage {

    /**
     * 追加 {@code event}。重复的 {@code eventId} 将被忽略。
     *
     * @param event 审计事件
     * @return 完成阶段
     */
    CompletionStage<Void> append(AuditEvent event);

    /**
     * 在 {@code timeout} 内刷新缓冲事件。
     *
     * @param timeout 刷新超时
     * @return 完成阶段
     */
    CompletionStage<Void> flush(Duration timeout);
}
