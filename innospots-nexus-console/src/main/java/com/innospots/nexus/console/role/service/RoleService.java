package com.innospots.nexus.console.role.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.operator.RoleBindingOperator;
import com.innospots.nexus.console.role.operator.RoleOperator;

/**
 * 角色与绑定管理工作流入口，子类可覆写方法以组合扩展 operator 行为。
 */
@RequiredArgsConstructor
public class RoleService {

    private final RoleOperator roleOperator;
    private final RoleBindingOperator roleBindingOperator;

    public PageResult<RoleVo> pageRoles(RolePageRequest request) {
        return roleOperator.pageRoles(request);
    }

    public RoleVo getRole(String roleId) {
        return roleOperator.getRole(roleId);
    }

    public RoleVo createRole(RoleCreateRequest request) {
        return roleOperator.createRole(request);
    }

    public RoleVo updateRole(String roleId, RoleUpdateRequest request) {
        return roleOperator.updateRole(roleId, request);
    }

    public void updateRoleStatus(String roleId, RoleStatusUpdateRequest request) {
        roleOperator.updateRoleStatus(roleId, request);
    }

    public void deleteRole(String roleId) {
        roleOperator.deleteRole(roleId);
    }

    public List<RoleOptionVo> listRoleOptions(BasicStatus status) {
        return roleOperator.listRoleOptions(status);
    }

    public PageResult<RoleBindingVo> pageRoleBindings(String roleId, RoleBindingPageRequest request) {
        return roleBindingOperator.pageRoleBindings(roleId, request);
    }

    public void addRoleBindings(String roleId, RoleBindingAddRequest request) {
        roleBindingOperator.addRoleBindings(roleId, request);
    }

    public void removeRoleBinding(String roleId, String bindingId) {
        roleBindingOperator.removeRoleBinding(roleId, bindingId);
    }
}
