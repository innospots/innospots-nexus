package com.innospots.nexus.platform.role.service;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleOperator;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;

/**
 * 运营平台角色服务：参数校验、PLATFORM 归属固化与会话边界，委托 {@link RoleOperator} 做基础持久化。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformRoleService {

    private final RoleOperator roleOperator;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return roleOperator.pageRoles(request, ownership);
    }

    public RoleVo getRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return roleOperator.getRole(roleId, ownership);
    }

    public RoleVo createRole(RoleCreateRequest request) {
        Objects.requireNonNull(request, "request");
        PlatformConsoleSession.requireOwnership();
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
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return roleOperator.updateRole(roleId, request, ownership);
    }

    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        roleOperator.updateRoleStatus(roleId, request, ownership);
    }

    public void deleteRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        roleOperator.deleteRole(roleId, ownership);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return roleOperator.listRoleOptions(status, ownership);
    }
}
