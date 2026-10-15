package com.innospots.nexus.platform.logger.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.operator.AuditLogOperator;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台审计日志查询：PLATFORM 会话与归属固化。
 */
@RequiredArgsConstructor
public class PlatformAuditLogService {

    private final AuditLogOperator auditLogOperator;

    public PageResult<AuditLogVo> pageAuditLogs(AuditLogPageRequest request) {
        requirePlatformSession();
        return auditLogOperator.pageLogs(request, platformOwnership());
    }

    public AuditLogVo getAuditLog(String auditLogId) {
        Checks.notBlank(auditLogId, "auditLogId");
        requirePlatformSession();
        return auditLogOperator.getLog(auditLogId, platformOwnership());
    }

    private void requirePlatformSession() {
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private ConsoleOwnership platformOwnership() {
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        return ownership;
    }
}
