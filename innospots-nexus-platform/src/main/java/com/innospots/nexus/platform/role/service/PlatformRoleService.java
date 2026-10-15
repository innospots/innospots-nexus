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
import com.innospots.nexus.platform.scope.PlatformConsoleSession;
import com.innospots.nexus.platform.scope.PlatformConsoleSession.Context;

/**
 * 运营平台角色服务：参数校验、PLATFORM 归属固化与会话边界，委托 {@link RoleOperator} 做基础持久化。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformRoleService {

    private final RoleOperator roleOperator;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        Context session = PlatformConsoleSession.requireContext();
        return roleOperator.pageRoles(
                request, session.ownership(), session.tenantId(), session.workspaceId());
    }

    public RoleVo getRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        Context session = PlatformConsoleSession.requireContext();
        return roleOperator.getRole(
                roleId, session.ownership(), session.tenantId(), session.workspaceId());
    }

    public RoleVo createRole(RoleCreateRequest request) {
        Objects.requireNonNull(request, "request");
        PlatformConsoleSession.requireContext();
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
        Context session = PlatformConsoleSession.requireContext();
        return roleOperator.updateRole(
                roleId, request, session.ownership(), session.tenantId(), session.workspaceId());
    }

    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        Checks.notBlank(roleId, "roleId");
        Objects.requireNonNull(request, "request");
        Context session = PlatformConsoleSession.requireContext();
        roleOperator.updateRoleStatus(
                roleId, request, session.ownership(), session.tenantId(), session.workspaceId());
    }

    public void deleteRole(String roleId) {
        Checks.notBlank(roleId, "roleId");
        Context session = PlatformConsoleSession.requireContext();
        roleOperator.deleteRole(
                roleId, session.ownership(), session.tenantId(), session.workspaceId());
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        Context session = PlatformConsoleSession.requireContext();
        return roleOperator.listRoleOptions(
                status, session.ownership(), session.tenantId(), session.workspaceId());
    }
}
