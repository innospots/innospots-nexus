package com.innospots.nexus.service.runtime.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.innospots.nexus.service.contract.audit.AuditEvent;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;

/**
 * 从调用上下文构造 {@link AuditEvent}。
 */
public final class AuditEvents {

    private AuditEvents() {
    }

    /**
     * 构造终态审计事件。
     *
     * @param invocation 调用上下文
     * @param outcome    终态结果
     * @return 审计事件
     */
    public static AuditEvent from(InvocationContext invocation, InvocationOutcome outcome) {
        String action = invocation.policy().auditAction();
        if (action == null || action.isBlank()) {
            action = invocation.operationId();
        }
        String resourceType = invocation.policy().auditResourceType();
        if (resourceType == null || resourceType.isBlank()) {
            resourceType = invocation.resource().type();
        }
        return new AuditEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                invocation.service().security(),
                invocation.service().scope(),
                action,
                resourceType,
                invocation.resource().id(),
                invocation.service().requestId(),
                invocation.service().trace().traceId(),
                resultName(outcome.type()),
                Map.of(),
                Map.of(),
                Map.of("code", outcome.code()));
    }

    private static String resultName(OutcomeType type) {
        return switch (type) {
            case SUCCEEDED -> "SUCCEEDED";
            case FAILED -> "FAILED";
            case CANCELLED -> "CANCELLED";
            case TIMED_OUT -> "TIMED_OUT";
            case REJECTED -> "REJECTED";
        };
    }
}
