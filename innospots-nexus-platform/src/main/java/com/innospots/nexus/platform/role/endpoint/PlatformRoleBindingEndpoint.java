package com.innospots.nexus.platform.role.endpoint;

import jakarta.ws.rs.BeanParam;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.role.service.PlatformRoleBindingService;

/**
 * 运营平台角色绑定 REST 实现。
 */
@Path(PlatformConstant.ROLE_BINDINGS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformRole", description = "运营平台角色")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRoleBindingEndpoint implements RoleBindingEndpoint {

    private final PlatformRoleBindingService platformRoleBindingService;

    @Override
    @GET
    @Operation(operationId = "platformRoleBindingPage", summary = "分页查询角色绑定")
    public R<PageResult<RoleBindingVo>> pageRoleBindings(
            @PathParam("roleId") String roleId,
            @BeanParam RoleBindingPageRequest request) {
        return R.ok(platformRoleBindingService.pageRoleBindings(roleId, request));
    }

    @Override
    @POST
    @Operation(operationId = "platformRoleBindingAdd", summary = "新增角色绑定")
    public R<Void> addRoleBindings(
            @PathParam("roleId") String roleId,
            RoleBindingAddRequest request) {
        platformRoleBindingService.addRoleBindings(roleId, request);
        return R.ok();
    }

    @Override
    @DELETE
    @Path("/{bindingId}")
    @Operation(operationId = "platformRoleBindingRemove", summary = "移除角色绑定")
    public R<Void> removeRoleBinding(
            @PathParam("roleId") String roleId,
            @PathParam("bindingId") String bindingId) {
        platformRoleBindingService.removeRoleBinding(roleId, bindingId);
        return R.ok();
    }
}
