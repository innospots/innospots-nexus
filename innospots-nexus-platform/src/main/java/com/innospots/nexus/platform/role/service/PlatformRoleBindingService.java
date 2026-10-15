package com.innospots.nexus.platform.role.service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;

/**
 * 运营平台角色绑定服务：PLATFORM 会话与归属固化，委托 {@link RoleBindingOperator}。
 */
@RequiredArgsConstructor
public class PlatformRoleBindingService {

    private final RoleBindingOperator roleBindingOperator;

    public PageResult<RoleBindingVo> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        Checks.notBlank(roleId, "roleId");
        return roleBindingOperator.pageRoleBindings(
                roleId, request, PlatformConsoleSession.requireOwnership());
    }

    public void addRoleBindings(String roleId, RoleBindingAddRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        roleBindingOperator.addRoleBindings(
                roleId, request, PlatformConsoleSession.requireOwnership());
    }

    public void removeRoleBinding(String roleId, String bindingId) {
        Checks.notBlank(roleId, "roleId");
        Checks.notBlank(bindingId, "bindingId");
        roleBindingOperator.removeRoleBinding(
                roleId, bindingId, PlatformConsoleSession.requireOwnership());
    }
}
