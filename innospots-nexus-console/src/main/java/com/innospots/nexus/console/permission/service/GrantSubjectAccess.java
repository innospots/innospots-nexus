package com.innospots.nexus.console.permission.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.permission.api.OrganizationUnitDirectory;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 校验授权主体属于给定控制台归属上下文。
 */
public final class GrantSubjectAccess {

    private final RoleDao roleDao;
    private final OrganizationUnitDirectory organizationUnitDirectory;

    public GrantSubjectAccess(RoleDao roleDao, OrganizationUnitDirectory organizationUnitDirectory) {
        this.roleDao = Checks.notNull(roleDao, "roleDao");
        this.organizationUnitDirectory = organizationUnitDirectory;
    }

    public void ensureSubjectInScope(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Checks.notNull(subjectType, "subjectType");
        Checks.notBlank(subjectId, "subjectId");
        Checks.notNull(ownership, "ownership");
        if (subjectType == PermissionSubjectType.ROLE) {
            ensureRoleInScope(subjectId, ownership, tenantId, workspaceId);
            return;
        }
        if (subjectType == PermissionSubjectType.ORG_UNIT) {
            ensureOrganizationUnitInTenant(subjectId, ownership, tenantId);
            return;
        }
        throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
    }

    private void ensureRoleInScope(
            String roleId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        RoleEntity role = roleDao.selectOne(ConsoleOwnershipScope.applyRoleListVisibility(
                Wrappers.<RoleEntity>lambdaQuery().eq(RoleEntity::getRoleId, roleId),
                ownership,
                tenantId,
                workspaceId));
        if (role == null) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        ConsoleOwnershipScope.assertRoleReadable(role, tenantId, ownership);
    }

    private void ensureOrganizationUnitInTenant(String unitId, ConsoleOwnership ownership, String tenantId) {
        if (organizationUnitDirectory == null) {
            return;
        }
        if (ownership.ownerType() != RoleOwnerType.WORKSPACE) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (tenantId == null || !organizationUnitDirectory.existsInTenant(tenantId, unitId)) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }
}
