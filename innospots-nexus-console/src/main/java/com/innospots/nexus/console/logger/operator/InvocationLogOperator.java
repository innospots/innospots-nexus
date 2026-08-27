package com.innospots.nexus.console.logger.operator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;
import com.innospots.nexus.console.logger.dao.AuditLogDao;
import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;

/**
 * Audit log data operator that persists intercepted invocations into the
 * audit-log domain.
 * <p>Receives a framework-independent {@link InvocationLogContext} assembled by
 * an interceptor adapter, maps it onto an {@link AuditLogEntity}, and writes it
 * through {@link AuditLogDao}. The operator owns no business workflow: it only
 * translates the context into the domain's persistence model.</p>
 *
 * @see InvocationLogContext
 * @see AuditLogEntity
 */
@Slf4j
@RequiredArgsConstructor
public class InvocationLogOperator {

    private static final String EXECUTION_SUCCESS = "SUCCESS";
    private static final String EXECUTION_FAILURE = "FAILURE";
    private static final int MESSAGE_MAX_LENGTH = 512;

    private final AuditLogDao auditLogDao;

    /**
     * Persists an intercepted invocation as an audit-log record.
     * <p>Audit records are append-only and must survive independently of the
     * audited operation's outcome, so this write intentionally runs without a
     * declarative transaction and never joins the surrounding one.</p>
     *
     * @param context assembled invocation data, never null
     */
    public void record(InvocationLogContext context) {
        if (context == null) {
            return;
        }
        AuditLogEntity entity = new AuditLogEntity();
        entity.setAction(context.action());
        entity.setPath(buildPath(context));
        entity.setOperatedTime(toLocalDateTime(context.startTime()));
        entity.setActor(context.actor());
        entity.setExecutionResult(context.success() ? EXECUTION_SUCCESS : EXECUTION_FAILURE);
        entity.setMessage(buildMessage(context));
        entity.setKeyParameters(serializeArguments(context.arguments()));
        auditLogDao.insert(entity);
    }

    private String buildPath(InvocationLogContext context) {
        return context.className() + "#" + context.methodName();
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    private String buildMessage(InvocationLogContext context) {
        Throwable exception = context.exception();
        if (exception != null) {
            String detail = exception.getMessage();
            return truncate(exception.getClass().getName()
                    + (detail != null ? ": " + detail : ""));
        }
        Object result = context.result();
        if (result != null) {
            return truncate(result.toString());
        }
        return null;
    }

    private String serializeArguments(Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            return null;
        }
        try {
            return Jsons.toJson(arguments);
        } catch (RuntimeException e) {
            // Argument payloads may hold non-serializable types; a failed
            // serialization must not break audit persistence.
            log.warn("Failed to serialize invocation arguments for audit log", e);
            return null;
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MESSAGE_MAX_LENGTH
                ? value
                : value.substring(0, MESSAGE_MAX_LENGTH);
    }
}
