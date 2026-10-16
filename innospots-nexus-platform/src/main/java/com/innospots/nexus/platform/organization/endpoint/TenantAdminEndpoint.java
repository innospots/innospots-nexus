package com.innospots.nexus.platform.organization.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.organization.domain.request.TenantAdminAddRequest;
import com.innospots.nexus.platform.organization.domain.vo.TenantAdminVo;
import com.innospots.nexus.platform.organization.service.PlatformTenantAdminService;

/**
 * 租户管理员列表、按邮箱添加与移除。
 */
@Path(PlatformConstant.TENANT_ADMINS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformOrganization", description = "组织开通与生命周期")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class TenantAdminEndpoint {

    private final PlatformTenantAdminService tenantAdminService;

    @GET
    @Operation(operationId = "platformTenantAdminList", summary = "列出租户管理员")
    public R<List<TenantAdminVo>> listAdmins(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId) {
        return R.ok(tenantAdminService.listAdmins(tenantId));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(operationId = "platformTenantAdminAdd", summary = "按邮箱添加租户管理员")
    public R<TenantAdminVo> addAdmin(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId,
            TenantAdminAddRequest request) {
        return R.ok(tenantAdminService.addAdminByEmail(tenantId, request));
    }

    @DELETE
    @Path("/{tenantAdminId}")
    @Operation(operationId = "platformTenantAdminRemove", summary = "移除租户管理员")
    public R<Void> removeAdmin(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId,
            @Parameter(description = "管理员记录 ID", required = true) @PathParam("tenantAdminId") String tenantAdminId) {
        tenantAdminService.removeAdmin(tenantId, tenantAdminId);
        return R.ok();
    }
}
