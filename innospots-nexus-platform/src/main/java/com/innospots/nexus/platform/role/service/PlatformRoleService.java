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
import com.innospots.nexus.platform.support.PlatformMutationLog;

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
        RoleVo created = roleOperator.createRole(
                roleName.trim(),
                roleCode.trim(),
                request.description(),
                RoleOwnerType.PLATFORM,
                null);
        PlatformMutationLog.info(
                log,
                "role created roleId={} roleCode={}",
                created.roleId(),
                created.roleCode());
        return created;
    }

    public RoleVo updateRole(String roleId, RoleUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        RoleVo updated = roleOperator.updateRole(roleId, request, ownership);
        PlatformMutationLog.info(log, "role updated roleId={} roleCode={}", updated.roleId(), updated.roleCode());
        return updated;
    }

    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        roleOperator.updateRoleStatus(roleId, request, ownership);
        PlatformMutationLog.info(
                log,
                "role status updated roleId={} status={}",
                roleId,
                request.status());
    }

    public void deleteRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        roleOperator.deleteRole(roleId, ownership);
        PlatformMutationLog.info(log, "role deleted roleId={}", roleId);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return roleOperator.listRoleOptions(status, ownership);
    }
}
