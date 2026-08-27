package com.innospots.nexus.platform.tenant.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.tenant.domain.request.TenantCreateRequest;
import com.innospots.nexus.platform.tenant.domain.vo.TenantVo;

/**
 * Ops-domain contract for tenant lifecycle.
 */
@Path("/platform/tenants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TenantEndpoint {

    /**
     * Opens a tenant and its enterprise profile.
     *
     * @param request tenant and enterprise creation data
     * @return created tenant summary
     */
    @POST
    R<TenantVo> createTenant(TenantCreateRequest request);

    /**
     * Returns one tenant.
     *
     * @param tenantId tenant identifier
     * @return tenant summary
     */
    @GET
    @Path("/{tenantId}")
    R<TenantVo> getTenant(@PathParam("tenantId") String tenantId);
}
