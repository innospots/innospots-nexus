package com.innospots.nexus.console.permission.endpoint;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 当前用户可见权限资源 REST 契约。
 */
@EndpointModule("permission")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
@NexusAuthenticatedApi
public interface CurrentAuthorizationEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "permission.visible.list",
            label = @I18nLiteral(pairs = {"zh-CN", "当前用户可见权限资源", "en", "List visible permission resources"}),
            ignorePermission = true)
    @Operation(operationId = "permissionVisibleList", summary = "当前用户可见权限资源")
    R<List<PermissionResourceVo>> listResources();
}
