package com.innospots.nexus.platform.role.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.endpoint.RoleBindingEndpoint;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.role.service.PlatformRoleBindingService;

/**
 * 运营平台角色绑定 REST 实现。
 */
@Path(PlatformConstant.ROLE_BINDINGS_PATH)
@Tag(name = "PlatformRole", description = "运营平台角色")
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
