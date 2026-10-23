package com.innospots.nexus.platform.role.endpoint;

import java.util.List;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.endpoint.RoleEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.role.service.PlatformRoleService;

/**
 * 运营平台角色 REST 实现：路径挂在 {@link PlatformConstant#ROLES_PATH}，归属由 {@link PlatformRoleService} 固化为 PLATFORM。
 */
@Path(PlatformConstant.ROLES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformRole", description = "运营平台角色")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRoleEndpoint implements RoleEndpoint {

    private final PlatformRoleService platformRoleService;

    @Override
    @GET
    @Operation(operationId = "platformRolePage", summary = "分页查询角色")
    public R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request) {
        return R.ok(platformRoleService.pageRoles(request));
    }

    @Override
    @GET
    @Path("/{roleId}")
    @Operation(operationId = "platformRoleGet", summary = "查询角色详情")
    public R<RoleVo> getRole(@PathParam("roleId") String roleId) {
        return R.ok(platformRoleService.getRole(roleId));
    }

    @Override
    @POST
    @Operation(operationId = "platformRoleCreate", summary = "创建角色")
    public R<RoleVo> createRole(RoleCreateRequest request) {
        return R.ok(platformRoleService.createRole(request));
    }

    @Override
    @PUT
    @Path("/{roleId}")
    @Operation(operationId = "platformRoleUpdate", summary = "更新角色")
    public R<RoleVo> updateRole(
            @PathParam("roleId") String roleId,
            RoleUpdateRequest request) {
        return R.ok(platformRoleService.updateRole(roleId, request));
    }

    @Override
    @PUT
    @Path("/{roleId}/status")
    @Operation(operationId = "platformRoleUpdateStatus", summary = "更新角色状态")
    public R<Void> updateRoleStatus(
            @PathParam("roleId") String roleId,
            RoleStatusUpdateRequest request) {
        platformRoleService.updateRoleStatus(roleId, request);
        return R.ok();
    }

    @Override
    @DELETE
    @Path("/{roleId}")
    @Operation(operationId = "platformRoleDelete", summary = "删除角色")
    public R<Void> deleteRole(@PathParam("roleId") String roleId) {
        platformRoleService.deleteRole(roleId);
        return R.ok();
    }

    @Override
    @GET
    @Path("/options")
    @Operation(operationId = "platformRoleListOptions", summary = "角色下拉选项")
    public R<List<RoleOptionVo>> listRoleOptions(@QueryParam("status") BasicStatus status) {
        return R.ok(platformRoleService.listRoleOptions(status));
    }
}
