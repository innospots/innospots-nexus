package com.innospots.nexus.platform.role.service;

import java.util.Objects;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台角色绑定服务：PLATFORM 会话与归属固化，委托 {@link RoleBindingOperator}。
 */
@RequiredArgsConstructor
public class PlatformRoleBindingService {

    private final RoleBindingOperator roleBindingOperator;

    public PageResult<RoleBindingVo> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        Checks.notBlank(roleId, "roleId");
        return roleBindingOperator.pageRoleBindings(roleId, request, requirePlatformOwnership());
    }

    public void addRoleBindings(String roleId, RoleBindingAddRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        roleBindingOperator.addRoleBindings(roleId, request, requirePlatformOwnership());
    }

    public void removeRoleBinding(String roleId, String bindingId) {
        Checks.notBlank(roleId, "roleId");
        Checks.notBlank(bindingId, "bindingId");
        roleBindingOperator.removeRoleBinding(roleId, bindingId, requirePlatformOwnership());
    }

    private ConsoleOwnership requirePlatformOwnership() {
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        return ownership;
    }
}
