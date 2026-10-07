package com.innospots.nexus.platform.organization.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
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
import com.innospots.nexus.platform.organization.domain.request.EnterpriseProfileUpsertRequest;
import com.innospots.nexus.platform.organization.domain.vo.EnterpriseProfileVo;
import com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService;

/**
 * 租户下企业法定档案的 Jakarta REST 资源，与租户开通接口分离。
 * <p>路径 {@link com.innospots.nexus.platform.config.PlatformConstant#TENANT_ENTERPRISE_PATH}；
 * 团队或个人形态租户也可后续补录企业档案，业务规则由上层策略约束。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService
 * @see TenantEndpoint
 */
@Path(PlatformConstant.TENANT_ENTERPRISE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformEnterpriseProfile", description = "企业法定档案")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class EnterpriseProfileEndpoint {

    private final PlatformEnterpriseProfileService enterpriseProfileService;

    @GET
    @Operation(operationId = "platformTenantEnterpriseGet", summary = "查询企业法定档案")
    public R<EnterpriseProfileVo> getEnterpriseProfile(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId) {
        return R.ok(enterpriseProfileService.getEnterpriseProfile(tenantId));
    }

    @PUT
    @Operation(operationId = "platformTenantEnterpriseUpsert", summary = "创建或更新企业法定档案")
    public R<EnterpriseProfileVo> upsertEnterpriseProfile(
            @Parameter(description = "租户 ID", required = true) @PathParam("tenantId") String tenantId,
            EnterpriseProfileUpsertRequest request) {
        return R.ok(enterpriseProfileService.upsertEnterpriseProfile(tenantId, request));
    }
}
