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
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 角色权限全量替换 REST 契约。
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
@NexusAuthenticatedApi
public interface GrantManagementEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "permission.grant.role.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询角色授权", "en", "Get role permissions"}))
    @Operation(operationId = "permissionGrantListByRole", summary = "查询角色授权")
    R<PermissionGrantReplaceRequest> getRolePermissions(@PathParam("roleId") String roleId);

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.REPLACE,
            name = "permission.grant.role.replace",
            label = @I18nLiteral(pairs = {"zh-CN", "全量替换角色授权", "en", "Replace role permissions"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "permissionGrantReplaceByRole", summary = "全量替换角色授权")
    R<Void> replaceRolePermissions(
            @PathParam("roleId") String roleId,
            PermissionGrantReplaceRequest request
    );
}
