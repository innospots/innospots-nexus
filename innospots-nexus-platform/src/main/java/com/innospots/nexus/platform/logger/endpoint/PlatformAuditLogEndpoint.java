package com.innospots.nexus.platform.logger.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.endpoint.AuditLogEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.logger.service.PlatformAuditLogService;

@EndpointModule(value = "logger", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
@Path(PlatformConstant.AUDIT_LOGS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAuditLog", description = "运营平台审计日志")
@NexusAuthenticatedApi
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
