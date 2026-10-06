package com.innospots.nexus.platform.organization.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.platform.organization.domain.enums.TenantStatus;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;

/**
 * 平台租户分页查询的 JAX-RS Bean 参数（{@code GET /api/platform/tenants}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint#pageTenants
 */
@Schema(name = "TenantPageRequest", description = "分页查询平台租户")
public final class TenantPageRequest {

    @Schema(description = "租户名称或编码的模糊匹配")
    @QueryParam("input")
    private String input;

    @Schema(description = "生命周期状态")
    @QueryParam("status")
    private TenantStatus status;

    @Schema(description = "组织形态")
    @QueryParam("tenantType")
    private TenantType tenantType;

    @Schema(description = "从 1 开始的页码，默认 1")
    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @Schema(description = "分页大小，默认 20")
    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public TenantPageRequest() {
    }

    public String input() {
        return input;
    }

    public TenantStatus status() {
        return status;
    }

    public TenantType tenantType() {
        return tenantType;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}
