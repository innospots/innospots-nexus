package com.innospots.nexus.platform.role.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

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
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.role.service.PlatformRoleService;

/**
 * 运营平台角色 REST 实现：路径挂在 {@link PlatformConstant#ROLES_PATH}，归属由 {@link PlatformRoleService} 固化为 PLATFORM。
 */
@EndpointModule(value = "role", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
@Path(PlatformConstant.ROLES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformRole", description = "运营平台角色")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRoleEndpoint implements RoleEndpoint {

    private final PlatformRoleService platformRoleService;

    @Override
    public R<PageResult<RoleVo>> pageRoles(RolePageRequest request) {
        return R.ok(platformRoleService.pageRoles(request));
    }

    @Override
    public R<RoleVo> getRole(String roleId) {
        return R.ok(platformRoleService.getRole(roleId));
    }

    @Override
    public R<RoleVo> createRole(RoleCreateRequest request) {
        return R.ok(platformRoleService.createRole(request));
    }

    @Override
    public R<RoleVo> updateRole(String roleId, RoleUpdateRequest request) {
        return R.ok(platformRoleService.updateRole(roleId, request));
    }

    @Override
    public R<Void> updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        platformRoleService.updateRoleStatus(roleId, request);
        return R.ok();
    }

    @Override
    public R<Void> deleteRole(String roleId) {
        platformRoleService.deleteRole(roleId);
        return R.ok();
    }

    @Override
    public R<List<RoleOptionVo>> listRoleOptions(BasicStatus status) {
        return R.ok(platformRoleService.listRoleOptions(status));
    }
}
