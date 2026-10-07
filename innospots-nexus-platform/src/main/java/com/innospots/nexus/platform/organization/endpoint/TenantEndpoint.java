package com.innospots.nexus.platform.organization.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.organization.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantPageRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantStatusUpdateRequest;
import com.innospots.nexus.platform.organization.domain.request.TenantUpdateRequest;
import com.innospots.nexus.platform.organization.domain.vo.TenantVo;
import com.innospots.nexus.platform.organization.service.PlatformTenantService;

/**
 * 平台租户开通、分页查询与生命周期管理的 Jakarta REST 资源。
 * <p>路径前缀 {@link com.innospots.nexus.platform.config.PlatformConstant#TENANTS_PATH}；
 * 企业法定档案见 {@link EnterpriseProfileEndpoint}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.service.PlatformTenantService
 * @see EnterpriseProfileEndpoint
 */
@Path(PlatformConstant.TENANTS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformOrganization", description = "组织开通与生命周期")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class TenantEndpoint {

    private final PlatformTenantService platformTenantService;

    @GET
    @Operation(operationId = "platformTenantPage", summary = "分页查询租户")
    public R<PageResult<TenantVo>> pageTenants(@BeanParam TenantPageRequest request) {
        return R.ok(platformTenantService.pageTenants(request));
    }

    @POST
    @Operation(operationId = "platformTenantCreate", summary = "开通租户")
    public R<TenantVo> createTenant(TenantCreateRequest request) {
        return R.ok(platformTenantService.createTenant(request));
    }

    @GET
    @Path("/{tenantId}")
    @Operation(operationId = "platformTenantGet", summary = "查询租户概要")
    public R<TenantVo> getTenant(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId) {
        return R.ok(platformTenantService.getTenant(tenantId));
    }

    @PUT
    @Path("/{tenantId}")
    @Operation(operationId = "platformTenantUpdate", summary = "更新租户资料")
    public R<TenantVo> updateTenant(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId,
            TenantUpdateRequest request) {
        return R.ok(platformTenantService.updateTenant(tenantId, request));
    }

    @PUT
    @Path("/{tenantId}/status")
    @Operation(operationId = "platformTenantUpdateStatus", summary = "更新租户状态")
    public R<TenantVo> updateTenantStatus(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId,
            TenantStatusUpdateRequest request) {
        return R.ok(platformTenantService.updateTenantStatus(tenantId, request));
    }
}
