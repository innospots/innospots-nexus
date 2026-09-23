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
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 将被拦截调用持久化到审计日志领域的数据操作器。
 * <p>接收由拦截器适配器组装的框架无关 {@link InvocationLogContext}，
 * 将其映射到 {@link AuditLogEntity} 并写入
 * {@link AuditLogDao}。该 Operator 不拥有业务工作流：它仅
 * 将上下文转换为领域的持久化模型。</p>
 *
 * @author Smars
 * @date 2026/09/13
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
     * 将被拦截调用持久化为审计日志记录。
     * <p>审计记录仅追加，且必须独立于
     * 被审计操作的结果而存在，因此本写入刻意在无
     * 声明式事务中运行且从不加入外层事务。</p>
     *
     * @param context 已组装的调用数据，永不为 null
     */
    public void record(InvocationLogContext context) {
        if (context == null) {
            return;
        }
        AuditLogEntity entity = new AuditLogEntity();
        ConsoleOwnershipScope.stamp(entity, ConsoleOwnershipScope.captureForAudit());
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
            // 参数载荷可能包含不可序列化类型；序列化失败
            // 不得破坏审计持久化。
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
