package com.innospots.nexus.platform.logger.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.console.logger.endpoint.AuditLogEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.logger.service.PlatformAuditLogService;

@Path(PlatformConstant.AUDIT_LOGS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAuditLog", description = "运营平台审计日志")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformAuditLogEndpoint implements AuditLogEndpoint {

    private final PlatformAuditLogService platformAuditLogService;

    @Override
    @GET
    @Operation(operationId = "platformAuditLogPage", summary = "分页查询审计日志")
    public R<PageResult<AuditLogVo>> pageAuditLogs(@BeanParam AuditLogPageRequest request) {
        return R.ok(platformAuditLogService.pageAuditLogs(request));
    }

    @Override
    @GET
    @Path("/{auditLogId}")
    @Operation(operationId = "platformAuditLogGet", summary = "查询审计日志详情")
    public R<AuditLogVo> getAuditLog(@PathParam("auditLogId") String auditLogId) {
        return R.ok(platformAuditLogService.getAuditLog(auditLogId));
    }
}
