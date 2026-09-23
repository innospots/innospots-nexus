package com.innospots.nexus.console.permission.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GrantSubjectAccessTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void ensureSubjectInScopeRejectsRoleOutsideWorkspace() {
        bindWorkspaceScope("tnt-a", "wks-a");
        RoleDao roleDao = mock(RoleDao.class);
        when(roleDao.selectOne(any())).thenReturn(null);
        GrantSubjectAccess access = new GrantSubjectAccess(roleDao, null);

        assertThatThrownBy(() -> access.ensureSubjectInScope(PermissionSubjectType.ROLE, "rol-x"))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void ensureSubjectInScopeAcceptsScopedRole() {
        bindWorkspaceScope("tnt-a", "wks-a");
        RoleEntity role = new RoleEntity();
        role.setRoleId("rol-1");
        role.setOwnerType(RoleOwnerType.WORKSPACE.name());
        role.setOwnerId("wks-a");
        role.setSecurityRealm(SecurityRealm.TENANT.name());
        RoleDao roleDao = mock(RoleDao.class);
        when(roleDao.selectOne(any())).thenReturn(role);
        GrantSubjectAccess access = new GrantSubjectAccess(roleDao, null);

        access.ensureSubjectInScope(PermissionSubjectType.ROLE, "rol-1");
    }

    private static void bindWorkspaceScope(String tenantId, String workspaceId) {
        TenantSnapshot tenant = new TenantSnapshot(tenantId, "code", "Tenant", BasicStatus.ENABLED);
        OrganizationSnapshot organization = new OrganizationSnapshot(
                tenantId, "org", "Org", "zh-CN", "CNY", null, BasicStatus.ENABLED);
        SessionContext.bindTenant(tenant, organization);
        SessionContext.bindWorkspace(new WorkspaceSnapshot(
                tenantId, workspaceId, "main", "Main", BasicStatus.ENABLED));
    }
}
