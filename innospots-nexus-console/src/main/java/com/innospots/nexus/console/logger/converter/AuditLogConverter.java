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
        return new AuditLogVo(
                entity.getAuditLogId(),
                occurredAt,
                entity.getActor(),
                entity.getActor(),
                entity.getSecurityRealm(),
                entity.getAction(),
                entity.getPath(),
                entity.getPath(),
                entity.getExecutionResult(),
                null,
                entity.getStatusCode(),
                entity.getMessage(),
                entity.getKeyParameters());
    }
}
