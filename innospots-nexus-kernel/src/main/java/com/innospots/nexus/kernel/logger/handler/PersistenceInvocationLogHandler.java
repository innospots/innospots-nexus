package com.innospots.nexus.kernel.logger.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.kernel.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.kernel.logger.InvocationLogHandler;
import com.innospots.nexus.kernel.logger.operator.InvocationLogOperator;

/**
 * Persistence-backed implementation of {@link InvocationLogHandler}.
 * <p>Adapts a completed {@link InvocationLogContext} to the audit log operator,
 * which stores it in the audit-log domain. A persistence failure is logged but
 * never propagated, because audit logging must not disturb the audited
 * operation — this handler is invoked from an interceptor's {@code finally}
 * block.</p>
 *
 * @see InvocationLogHandler
 * @see InvocationLogOperator
 */
@Slf4j
@RequiredArgsConstructor
public class PersistenceInvocationLogHandler implements InvocationLogHandler {

    private final InvocationLogOperator operator;

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
