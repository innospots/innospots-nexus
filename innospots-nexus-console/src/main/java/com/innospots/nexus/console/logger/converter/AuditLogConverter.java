package com.innospots.nexus.console.logger.converter;

import java.time.format.DateTimeFormatter;

import com.innospots.nexus.console.logger.domain.entity.AuditLogEntity;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;

/**
 * {@link AuditLogEntity} 与 {@link AuditLogVo} 转换。
 */
public final class AuditLogConverter {

    private static final DateTimeFormatter OCCURRED_AT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private AuditLogConverter() {
    }

    public static AuditLogVo toVo(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        String occurredAt = entity.getOperatedTime() == null
                ? null
                : entity.getOperatedTime().format(OCCURRED_AT);
        String target = entity.getTarget();
        if (target == null || target.isBlank()) {
            // 旧记录曾将 EndpointGrant.name 存入 action；回退展示可避免历史对象列丢失。
            target = entity.getAction();
        }
        return new AuditLogVo(
                entity.getAuditLogId(),
                occurredAt,
                entity.getActor(),
                entity.getActor(),
                entity.getSecurityRealm(),
                entity.getAction(),
                target,
                entity.getPath(),
                entity.getExecutionResult(),
                entity.getDurationMs(),
                entity.getStatusCode(),
                entity.getMessage(),
                entity.getFailureDetail(),
                entity.getKeyParameters());
    }
}
