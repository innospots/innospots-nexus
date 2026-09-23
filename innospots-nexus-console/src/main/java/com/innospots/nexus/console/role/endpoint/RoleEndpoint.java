package com.innospots.nexus.console.role.endpoint;

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

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.service.RoleService;

/**
 * 角色生命周期与查询 REST 资源，可直接继承以扩展路由或响应包装。
 */
@Path("/console/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class RoleEndpoint {

    private final RoleService roleService;

    @GET
    public R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request) {
        return R.ok(roleService.pageRoles(request));
    }

    @GET
    @Path("/{roleId}")
    public R<RoleVo> getRole(@PathParam("roleId") String roleId) {
        return R.ok(roleService.getRole(roleId));
    }

    @POST
    public R<RoleVo> createRole(RoleCreateRequest request) {
        return R.ok(roleService.createRole(request));
    }

    @PUT
    @Path("/{roleId}")
    public R<RoleVo> updateRole(
            @PathParam("roleId") String roleId,
            RoleUpdateRequest request) {
        return R.ok(roleService.updateRole(roleId, request));
    }

    @PUT
    @Path("/{roleId}/status")
    public R<Void> updateRoleStatus(
            @PathParam("roleId") String roleId,
            RoleStatusUpdateRequest request) {
        roleService.updateRoleStatus(roleId, request);
        return R.ok();
    }

    @DELETE
    @Path("/{roleId}")
    public R<Void> deleteRole(@PathParam("roleId") String roleId) {
        roleService.deleteRole(roleId);
        return R.ok();
    }

    @GET
    @Path("/options")
    public R<List<RoleOptionVo>> listRoleOptions(@QueryParam("status") BasicStatus status) {
        return R.ok(roleService.listRoleOptions(status));
    }
}
