package com.innospots.nexus.platform.logger.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.endpoint.AuditLogEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.logger.service.PlatformAuditLogService;

@Path(PlatformConstant.AUDIT_LOGS_PATH)
@Tag(name = "PlatformAuditLog", description = "运营平台审计日志")
@RequiredArgsConstructor
public class PlatformAuditLogEndpoint implements AuditLogEndpoint {

    private final PlatformAuditLogService platformAuditLogService;

    @Override
    public R<PageResult<AuditLogVo>> pageAuditLogs(AuditLogPageRequest request) {
        return R.ok(platformAuditLogService.pageAuditLogs(request));
    }

    @Override
    public R<AuditLogVo> getAuditLog(String auditLogId) {
        return R.ok(platformAuditLogService.getAuditLog(auditLogId));
    }
}
