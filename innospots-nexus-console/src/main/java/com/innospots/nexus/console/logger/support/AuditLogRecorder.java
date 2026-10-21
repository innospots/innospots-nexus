package com.innospots.nexus.console.logger.support;

import com.innospots.nexus.console.endpoint.grant.support.ResolvedEndpointGrant;
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
        if (state == null || state.grant() == null || !state.grant().auditLog()) {
            return;
        }
        ResolvedEndpointGrant grant = state.grant();
        long endTime = System.currentTimeMillis();
        boolean success = failure == null && httpStatus > 0 && httpStatus < 400;
        Throwable capturedException = failure;
        Object capturedResult = null;
        Object[] arguments = grant.recordArgs() ? new Object[0] : null;
        String action = grant.action() == null ? "" : grant.action().name();
        if (action.isBlank()) {
            action = state.methodName();
        }
        String operationMessage = AuditLogMessages.resolveOperationMessage(grant.label());
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
                httpPath,
                grant.name());
        handler.handle(context);
    }
}
