package com.innospots.nexus.console.logger;

import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

/**
 * 通用拦截处理器端口。
 * <p>接收完整组装的 {@link InvocationLogContext}，负责将其持久化或转发。
 * 实现保持框架无关，例如可通过 {@code AuditLogDao} 写入 {@code AuditLogEntity}，
 * 或将上下文发布到其他接收端。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@FunctionalInterface
public interface InvocationLogHandler {

    /**
     * 处理已完成的调用上下文。
     *
     * @param context 已组装的调用数据，永不为 null
     */
    void handle(InvocationLogContext context);
}
