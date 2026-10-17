package com.innospots.nexus.console.logger.support;

import com.innospots.nexus.console.logger.AuditLog;
import com.innospots.nexus.console.logger.AuditLogMessages;
import com.innospots.nexus.console.logger.InvocationLogHandler;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

/**
 * 将已完成的 HTTP 调用组装为 {@link InvocationLogContext} 并委托处理器。
 */
public final class AuditLogRecorder {

    private final InvocationLogHandler handler;

    public AuditLogRecorder(InvocationLogHandler handler) {
        this.handler = java.util.Objects.requireNonNull(handler, "handler");
    }

    public void record(
            AuditLogInvocationState state,
            String actor,
            int httpStatus,
            Object responseEntity,
            Throwable failure
    ) {
        if (state == null || state.auditLog() == null) {
            return;
        }
        AuditLog auditLog = state.auditLog();
        long endTime = System.currentTimeMillis();
        boolean success = failure == null && httpStatus > 0 && httpStatus < 400;
        Throwable capturedException = auditLog.recordException() ? failure : null;
        Object capturedResult = null;
        if (auditLog.recordResult() && success) {
            capturedResult = responseEntity;
        }
        Object[] arguments = auditLog.recordArgs() ? new Object[0] : null;
        String action = auditLog.action();
        if (action == null || action.isBlank()) {
            action = state.methodName();
        }
        String operationMessage = AuditLogMessages.resolveOperationMessage(auditLog);
        String httpPath = state.httpMethod().isEmpty()
                ? state.httpPath()
                : state.httpMethod() + " " + state.httpPath();
        InvocationLogContext context = new InvocationLogContext(
                state.className(),
                state.methodName(),
                action,
                arguments,
                capturedResult,
                capturedException,
                state.startTimeMillis(),
                endTime,
                actor,
                operationMessage,
                httpPath);
        handler.handle(context);
    }
}
