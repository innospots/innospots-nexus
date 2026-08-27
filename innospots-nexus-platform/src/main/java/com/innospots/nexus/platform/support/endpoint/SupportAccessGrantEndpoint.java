package com.innospots.nexus.platform.support.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.support.domain.request.SupportAccessGrantCreateRequest;
import com.innospots.nexus.platform.support.domain.vo.SupportAccessGrantVo;

/**
 * Ops-domain contract for time-bounded tenant support access.
 */
@Path("/platform/support-access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SupportAccessGrantEndpoint {

    /**
     * Creates a pending support-access grant.
     *
     * @param request grant creation data
     * @return created grant summary
     */
    @POST
    R<SupportAccessGrantVo> createGrant(SupportAccessGrantCreateRequest request);

    /**
     * Returns one support-access grant.
     *
     * @param grantId grant identifier
     * @return grant summary
     */
    @GET
    @Path("/{grantId}")
    R<SupportAccessGrantVo> getGrant(@PathParam("grantId") String grantId);
}
