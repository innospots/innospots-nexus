package com.innospots.nexus.platform.role.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.role.service.PlatformRoleBindingService;

/**
 * 运营平台角色绑定 REST 实现。
 */
@EndpointModule(value = "role", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
@Path(PlatformConstant.ROLE_BINDINGS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformRole", description = "运营平台角色")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRoleBindingEndpoint implements RoleBindingEndpoint {

    private final PlatformRoleBindingService platformRoleBindingService;

    @Override
    public R<PageResult<RoleBindingVo>> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        return R.ok(platformRoleBindingService.pageRoleBindings(roleId, request));
    }

    @Override
    public R<Void> addRoleBindings(String roleId, RoleBindingAddRequest request) {
        platformRoleBindingService.addRoleBindings(roleId, request);
        return R.ok();
    }

    @Override
    public R<Void> removeRoleBinding(String roleId, String bindingId) {
        platformRoleBindingService.removeRoleBinding(roleId, bindingId);
        return R.ok();
    }
}
