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

    /**
     * 在已解析的 {@link ConsoleOwnership} 下校验授权主体；PLATFORM 归属不依赖租户/工作区快照。
     */
    public void ensureSubjectInScope(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(ownership, "ownership");
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            ensureSubjectInScope(subjectType, subjectId, ownership, null, null);
            return;
        }
        String tenantId = resolveTenantId(ownership);
        String workspaceId = ownership.ownerType() == RoleOwnerType.WORKSPACE ? ownership.ownerId() : null;
        ensureSubjectInScope(subjectType, subjectId, ownership, tenantId, workspaceId);
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
        Checks.notBlank(unitId, "unitId");
        if (ownership.ownerType() == RoleOwnerType.PLATFORM) {
            ensureOrganizationUnitForPlatform(unitId, tenantId);
            return;
        }
        if (ownership.ownerType() != RoleOwnerType.WORKSPACE) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
        if (organizationUnitDirectory == null) {
            return;
        }
        if (tenantId == null || !organizationUnitDirectory.existsInTenant(tenantId, unitId)) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    /**
     * 运营平台管理组织单元授权：归属为 PLATFORM；目录未装配时仅校验 unitId 非空。
     */
    private void ensureOrganizationUnitForPlatform(String unitId, String tenantId) {
        if (organizationUnitDirectory == null) {
            return;
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
        }
        if (!organizationUnitDirectory.existsInTenant(tenantId, unitId)) {
            throw ConsoleExceptions.build(NexusStatusCode.NO_PERMISSION);
        }
    }

    private static String resolveTenantId(ConsoleOwnership ownership) {
        if (ownership.ownerType() == RoleOwnerType.TENANT) {
            return ownership.ownerId();
        }
        return com.innospots.nexus.base.thread.SessionContext.tenantId();
    }
}
