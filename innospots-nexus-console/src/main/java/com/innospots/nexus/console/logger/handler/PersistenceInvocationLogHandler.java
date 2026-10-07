package com.innospots.nexus.console.logger.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.console.logger.InvocationLogHandler;
import com.innospots.nexus.console.logger.operator.InvocationLogOperator;

/**
 * {@link InvocationLogHandler} 的持久化实现。
 * <p>将已完成的 {@link InvocationLogContext} 适配到审计日志操作器，
 * 后者将其存入审计日志领域。持久化失败会记录日志但
 * 永不传播，因为审计日志不得干扰被审计
 * 操作——本处理器从拦截器的 {@code finally}
 * 块中调用。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see InvocationLogHandler
 * @see InvocationLogOperator
 */
@Slf4j
@RequiredArgsConstructor
public class PersistenceInvocationLogHandler implements InvocationLogHandler {

    private final InvocationLogOperator operator;
    /**
     * 处理。
     * @param context 调用上下文
     */

    
    @Override
    public void handle(InvocationLogContext context) {
        try {
            operator.record(context);
        } catch (RuntimeException e) {
            log.warn("Failed to persist invocation audit log for action '{}' on {}.{}",
                    context.action(), context.className(), context.methodName(), e);
        }
    }
}
