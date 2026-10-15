package com.innospots.nexus.platform.role.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipLevel;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 运营平台角色服务：参数校验、PLATFORM 归属固化与会话边界，委托 {@link RoleOperator} 做基础持久化。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformRoleService {

    private final RoleOperator roleOperator;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        RoleSessionScope scope = requirePlatformSession();
        return roleOperator.pageRoles(request, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    public RoleVo getRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        RoleSessionScope scope = requirePlatformSession();
        return roleOperator.getRole(roleId, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    public RoleVo createRole(RoleCreateRequest request) {
        Objects.requireNonNull(request, "request");
        requirePlatformSession();
        String roleName = Checks.notBlank(request.roleName(), "roleName");
        String roleCode = Checks.notBlank(request.roleCode(), "roleCode");
        if (log.isDebugEnabled()) {
            log.debug("createRole platform-fixed ownerType=PLATFORM roleCode={}", roleCode);
        }
        return roleOperator.createRole(
                roleName.trim(),
                roleCode.trim(),
                request.description(),
                RoleOwnerType.PLATFORM,
                null);
    }

    public RoleVo updateRole(String roleId, RoleUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        RoleSessionScope scope = requirePlatformSession();
        return roleOperator.updateRole(
                roleId, request, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        RoleSessionScope scope = requirePlatformSession();
        roleOperator.updateRoleStatus(
                roleId, request, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    public void deleteRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        RoleSessionScope scope = requirePlatformSession();
        roleOperator.deleteRole(roleId, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        RoleSessionScope scope = requirePlatformSession();
        return roleOperator.listRoleOptions(
                status, scope.sessionOwnership(), scope.tenantId(), scope.workspaceId());
    }

    /**
     * 平台角色 API 仅接受 PLATFORM 会话；归属固定为 PLATFORM / securityRealm=PLATFORM。
     */
    protected RoleSessionScope requirePlatformSession() {
        ConsoleSessionScope sessionScope = ConsoleSessionContext.scope();
        if (sessionScope != ConsoleSessionScope.PLATFORM) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        ConsoleOwnership ownership = ConsoleOwnershipScope.requireOwnership(ConsoleOwnershipLevel.PLATFORM);
        if (ownership.ownerType() != RoleOwnerType.PLATFORM) {
            throw ConsoleExceptions.build(RoleStatusCode.INVALID_ROLE_OWNER);
        }
        return new RoleSessionScope(null, ownership);
    }

    /**
     * 角色 API 会话边界（由 service 持有；operator 不感知会话）。
     */
    protected record RoleSessionScope(String tenantId, ConsoleOwnership sessionOwnership) {

        String workspaceId() {
            return null;
        }
    }
}
