package com.innospots.nexus.console.logger.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.logger.domain.request.AuditLogPageRequest;
import com.innospots.nexus.console.logger.domain.vo.AuditLogVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 审计日志查询 REST 契约；类级 {@code @Path} 由 platform 实现绑定。
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "AuditLog", description = "审计日志")
@NexusAuthenticatedApi
public interface AuditLogEndpoint {

    @GET
    @Operation(operationId = "auditLogPage", summary = "分页查询审计日志")
    R<PageResult<AuditLogVo>> pageAuditLogs(@BeanParam AuditLogPageRequest request);

    @GET
    @Path("/{auditLogId}")
    @Operation(operationId = "auditLogGet", summary = "查询审计日志详情")
    R<AuditLogVo> getAuditLog(@PathParam("auditLogId") String auditLogId);
}
