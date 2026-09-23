package com.innospots.nexus.console.role.endpoint;

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

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.service.RoleService;

/**
 * 角色绑定 REST 资源。
 */
@Path("/console/roles/{roleId}/bindings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class RoleBindingEndpoint {

    private final RoleService roleService;

    @GET
    public R<PageResult<RoleBindingVo>> pageRoleBindings(
            @PathParam("roleId") String roleId,
            @BeanParam RoleBindingPageRequest request) {
        return R.ok(roleService.pageRoleBindings(roleId, request));
    }

    @POST
    public R<Void> addRoleBindings(
            @PathParam("roleId") String roleId,
            RoleBindingAddRequest request) {
        roleService.addRoleBindings(roleId, request);
        return R.ok();
    }

    @DELETE
    @Path("/{bindingId}")
    public R<Void> removeRoleBinding(
            @PathParam("roleId") String roleId,
            @PathParam("bindingId") String bindingId) {
        roleService.removeRoleBinding(roleId, bindingId);
        return R.ok();
    }
}
