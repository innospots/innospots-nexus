package com.innospots.nexus.console.permission.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.permission.api.OrganizationUnitDirectory;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 校验授权主体属于当前控制台归属上下文。
 */
public final class GrantSubjectAccess {

    private final RoleDao roleDao;
    private final OrganizationUnitDirectory organizationUnitDirectory;

    public GrantSubjectAccess(RoleDao roleDao, OrganizationUnitDirectory organizationUnitDirectory) {
        this.roleDao = Checks.notNull(roleDao, "roleDao");
        this.organizationUnitDirectory = organizationUnitDirectory;
    }

    public void ensureSubjectInScope(PermissionSubjectType subjectType, String subjectId) {
        Checks.notNull(subjectType, "subjectType");
        Checks.notBlank(subjectId, "subjectId");
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        if (subjectType == PermissionSubjectType.ROLE) {
            ensureRoleInScope(subjectId, ownership);
            return;
        }
        if (subjectType == PermissionSubjectType.ORG_UNIT) {
            ensureOrganizationUnitInTenant(subjectId, ownership);
            return;
        }
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER);
    }

    private void ensureRoleInScope(String roleId, ConsoleOwnership ownership) {
        String tenantId = Checks.notBlank(SessionContext.tenantId(), "tenantId");
        RoleEntity role = roleDao.selectOne(ConsoleOwnershipScope.applyRoleListVisibility(
                Wrappers.<RoleEntity>lambdaQuery().eq(RoleEntity::getRoleId, roleId),
                tenantId,
                ownership.ownerId(),
                ownership.securityRealm()));
        if (role == null) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        ConsoleOwnershipScope.assertRoleReadable(role, tenantId, ownership);
    }

    private void ensureOrganizationUnitInTenant(String unitId, ConsoleOwnership ownership) {
        if (organizationUnitDirectory == null) {
            return;
        }
        if (ownership.ownerType() != com.innospots.nexus.console.role.domain.enums.RoleOwnerType.WORKSPACE) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
        String tenantId = com.innospots.nexus.base.thread.SessionContext.tenantId();
        if (tenantId == null || !organizationUnitDirectory.existsInTenant(tenantId, unitId)) {
            throw NexusException.build(NexusStatusCode.NO_PERMISSION);
        }
    }
}
