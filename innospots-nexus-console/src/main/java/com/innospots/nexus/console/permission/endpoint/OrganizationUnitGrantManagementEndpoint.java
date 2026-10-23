package com.innospots.nexus.console.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 组织单元权限全量替换 REST 契约。
 */
@EndpointModule("permission")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
@NexusAuthenticatedApi
public interface OrganizationUnitGrantManagementEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "permission.grant.org-unit.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询组织单元授权", "en", "Get organization unit permissions"}))
    @Operation(operationId = "permissionGrantListByOrgUnit", summary = "查询组织单元授权")
    R<PermissionGrantReplaceRequest> getOrganizationUnitPermissions(@PathParam("unitId") String unitId);

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.REPLACE,
            name = "permission.grant.org-unit.replace",
            label = @I18nLiteral(pairs = {"zh-CN", "全量替换组织单元授权", "en", "Replace organization unit permissions"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "permissionGrantReplaceByOrgUnit", summary = "全量替换组织单元授权")
    R<Void> replaceOrganizationUnitPermissions(
            @PathParam("unitId") String unitId,
            PermissionGrantReplaceRequest request
    );
}
