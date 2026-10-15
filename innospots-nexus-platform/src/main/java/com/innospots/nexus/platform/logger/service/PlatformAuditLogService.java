package com.innospots.nexus.platform.logger.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.operator.AuditLogOperator;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;

/**
 * 运营平台审计日志查询：PLATFORM 会话与归属固化。
 */
@RequiredArgsConstructor
public class PlatformAuditLogService {

    private final AuditLogOperator auditLogOperator;

    public PageResult<AuditLogVo> pageAuditLogs(AuditLogPageRequest request) {
        return auditLogOperator.pageLogs(request, PlatformConsoleSession.requireOwnership());
    }

    public AuditLogVo getAuditLog(String auditLogId) {
        Checks.notBlank(auditLogId, "auditLogId");
        return auditLogOperator.getLog(auditLogId, PlatformConsoleSession.requireOwnership());
    }
}
