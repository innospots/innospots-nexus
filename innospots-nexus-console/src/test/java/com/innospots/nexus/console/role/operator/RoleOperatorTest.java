package com.innospots.nexus.console.role.operator;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.converter.RoleConverter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.console.role.status.RoleStatusCode;
import com.innospots.nexus.console.scope.ConsoleOwnership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

class RoleOperatorTest {

    @Test
    void pageRolesReturnsPlatformRoleForPlatformOwnership() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        RoleEntity platformRole = new RoleEntity();
        platformRole.setRoleId("rol-platform");
        platformRole.setRoleName("Platform Ops");
        platformRole.setRoleCode("platform-ops");
        platformRole.setOwnerType(RoleOwnerType.PLATFORM.name());
        platformRole.setSecurityRealm(SecurityRealm.PLATFORM.name());
        platformRole.setStatus(BasicStatus.ENABLED.name());
        IPage<RoleEntity> page = new Page<>(1, 20, 1);
        page.setRecords(List.of(platformRole));
        when(roleDao.selectPage(any(), any())).thenReturn(page);
        when(bindingDao.selectCount(any())).thenReturn(0L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);

        PageResult<RoleVo> result = operator.pageRoles(
                new RolePageRequest(), platformOwnership(), null, null);

        assertThat(result.records()).hasSize(1);
        assertThat(result.records().getFirst().roleCode()).isEqualTo("platform-ops");
    }

    @Test
    void createRoleRejectsPlatformOwnerIdWhenProvided() {
        RoleOperator operator = new RoleOperator(mock(RoleDao.class), mock(RoleBindingDao.class), RoleConverter.INSTANCE);

        assertThatThrownBy(() -> operator.createRole(
                "Platform Role", "platform-role", null, RoleOwnerType.PLATFORM, "wks-1"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(RoleStatusCode.INVALID_ROLE_OWNER.fullCode());
    }

    @Test
    void createRoleAllowsPlatformOwnerWithoutOwnerId() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        when(roleDao.selectCount(any())).thenReturn(0L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);
        ArgumentCaptor<RoleEntity> saved = ArgumentCaptor.forClass(RoleEntity.class);

        operator.createRole("Platform Role", "platform-role", null, RoleOwnerType.PLATFORM, null);

        verify(roleDao).insert(saved.capture());
        assertThat(saved.getValue().getOwnerId()).isNull();
        assertThat(saved.getValue().getSecurityRealm()).isEqualTo(SecurityRealm.PLATFORM.name());
    }

    @Test
    void createRoleDerivesSecurityRealmAndDefaultSortOrder() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        when(roleDao.selectCount(any())).thenReturn(0L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);
        ArgumentCaptor<RoleEntity> saved = ArgumentCaptor.forClass(RoleEntity.class);

        operator.createRole("Workspace Ops", "workspace-ops", null, RoleOwnerType.WORKSPACE, "wks-1");

        verify(roleDao).insert(saved.capture());
        assertThat(saved.getValue().getSecurityRealm()).isEqualTo(SecurityRealm.TENANT.name());
        assertThat(saved.getValue().getSortOrder()).isEqualTo(1);
        assertThat(saved.getValue().getOwnerId()).isEqualTo("wks-1");
    }

    @Test
    void createRoleRejectsDuplicateRoleCode() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        when(roleDao.selectCount(any())).thenReturn(1L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);

        assertThatThrownBy(() -> operator.createRole(
                "Admin", "admin", null, RoleOwnerType.WORKSPACE, "wks-1"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(RoleStatusCode.ROLE_CODE_DUPLICATED.fullCode());
    }

    @Test
    void deleteRoleRejectsBuiltInRole() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        RoleEntity builtIn = workspaceRole("rol-1", "wks-1");
        builtIn.setBuiltIn(true);
        when(roleDao.selectOne(any())).thenReturn(builtIn);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);
        ConsoleOwnership ownership = workspaceOwnership("wks-1");

        assertThatThrownBy(() -> operator.deleteRole("rol-1", ownership, "tnt-1", "wks-1"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(RoleStatusCode.ROLE_PROTECTED.fullCode());
    }

    @Test
    void updateRoleStatusPersistsEnabledState() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        RoleEntity entity = workspaceRole("rol-2", "wks-1");
        entity.setBuiltIn(false);
        entity.setStatus(BasicStatus.DISABLED.name());
        when(roleDao.selectOne(any())).thenReturn(entity);
        when(bindingDao.selectCount(any())).thenReturn(0L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);
        ConsoleOwnership ownership = workspaceOwnership("wks-1");

        operator.updateRoleStatus(
                "rol-2", new RoleStatusUpdateRequest(BasicStatus.ENABLED), ownership, "tnt-1", "wks-1");

        assertThat(entity.getStatus()).isEqualTo(BasicStatus.ENABLED.name());
        verify(roleDao).updateById(entity);
    }

    @Test
    void getRoleAllowsTenantScopedRoleInWorkspaceOwnership() {
        RoleDao roleDao = mock(RoleDao.class);
        RoleBindingDao bindingDao = mock(RoleBindingDao.class);
        RoleEntity tenantRole = new RoleEntity();
        tenantRole.setRoleId("rol-tenant");
        tenantRole.setOwnerType(RoleOwnerType.TENANT.name());
        tenantRole.setOwnerId("tnt-1");
        tenantRole.setSecurityRealm(SecurityRealm.TENANT.name());
        tenantRole.setRoleName("Tenant Admin");
        tenantRole.setRoleCode("tenant-admin");
        tenantRole.setStatus(BasicStatus.ENABLED.name());
        when(roleDao.selectOne(any())).thenReturn(tenantRole);
        when(bindingDao.selectCount(any())).thenReturn(0L);
        RoleOperator operator = new RoleOperator(roleDao, bindingDao, RoleConverter.INSTANCE);

        assertThat(operator.getRole(
                "rol-tenant", workspaceOwnership("wks-1"), "tnt-1", "wks-1").roleId())
                .isEqualTo("rol-tenant");
    }

    private static RoleEntity workspaceRole(String roleId, String workspaceId) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleId(roleId);
        entity.setOwnerType(RoleOwnerType.WORKSPACE.name());
        entity.setOwnerId(workspaceId);
        entity.setSecurityRealm(SecurityRealm.TENANT.name());
        return entity;
    }

    private static ConsoleOwnership platformOwnership() {
        return new ConsoleOwnership(RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());
    }

    private static ConsoleOwnership workspaceOwnership(String workspaceId) {
        return new ConsoleOwnership(RoleOwnerType.WORKSPACE, workspaceId, SecurityRealm.TENANT.name());
    }
}
