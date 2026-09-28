package com.innospots.nexus.platform.tenant.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.tenant.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.tenant.domain.vo.TenantVo;

/**
 * 租户生命周期的运维域契约（开通、查询）。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path(ConsoleConstant.PLATFORM_TENANTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformTenant", description = "租户生命周期")
@NexusAuthenticatedApi
public interface TenantEndpoint {

    /**
     * 开通租户及其企业档案。
     *
     * @param request 租户与企业创建数据
     * @return created tenant 概要
     */
    @POST
    @Operation(operationId = "platformTenantCreate", summary = "开通租户及企业档案")
    R<TenantVo> createTenant(TenantCreateRequest request);

    /**
     * 返回单个租户。
     *
     * @param tenantId tenant 标识符
     * @return tenant 概要
     */
    @GET
    @Path("/{tenantId}")
    @Operation(operationId = "platformTenantGet", summary = "查询租户概要")
    R<TenantVo> getTenant(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId);
}
