package com.innospots.nexus.console.bootstrap.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.role.domain.entity.RoleBindingEntity;
import com.innospots.nexus.console.role.domain.entity.RoleEntity;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link InitialUserRoleBindingService} 绑定语义测试（启动引导第 5 阶段）。
 */
class InitialUserRoleBindingServiceTest {

    private final AdminUserDao adminUserDao = mock(AdminUserDao.class);
    private final CredentialService credentialService = mock(CredentialService.class);
    private final RoleDao roleDao = mock(RoleDao.class);
    private final RoleBindingDao roleBindingDao = mock(RoleBindingDao.class);

    private final InitialUserSyncService userService =
            new InitialUserSyncService(adminUserDao, credentialService);
    private final InitialUserRoleBindingService service =
            new InitialUserRoleBindingService(userService, roleDao, roleBindingDao);

    @Test
    void bindsConfiguredRole() {
        when(adminUserDao.selectOne(any())).thenReturn(adminUser("ADM00000000001", "ops01"));
        when(roleDao.selectOne(any())).thenReturn(role("sys_admin", "ROL00000000001"));
        when(roleBindingDao.selectCount(any())).thenReturn(0L);

        InitialUserRoleBindingService.SyncResult result = service.sync(List.of(
                user("ops01", List.of("sys_admin"))));

        ArgumentCaptor<RoleBindingEntity> captor =
                ArgumentCaptor.forClass(RoleBindingEntity.class);
        verify(roleBindingDao).insert(captor.capture());
        RoleBindingEntity binding = captor.getValue();
        assertThat(binding.getRoleId()).isEqualTo("ROL00000000001");
        assertThat(binding.getSubjectType()).isEqualTo("USER");
        assertThat(binding.getSubjectId()).isEqualTo("ADM00000000001");
        assertThat(binding.getSecurityRealm()).isEqualTo("PLATFORM");
        assertThat(result.bound()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
    }

    @Test
    void skipsExistingBinding() {
        when(adminUserDao.selectOne(any())).thenReturn(adminUser("ADM00000000001", "ops01"));
        when(roleDao.selectOne(any())).thenReturn(role("sys_admin", "ROL00000000001"));
        when(roleBindingDao.selectCount(any())).thenReturn(1L);

        InitialUserRoleBindingService.SyncResult result = service.sync(List.of(
                user("ops01", List.of("sys_admin"))));

        verify(roleBindingDao, never()).insert(any(RoleBindingEntity.class));
        assertThat(result.bound()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    void failsWhenRoleNotConfigured() {
        when(adminUserDao.selectOne(any())).thenReturn(adminUser("ADM00000000001", "ops01"));
        when(roleDao.selectOne(any())).thenReturn(null);

        List<InitialUser> users = List.of(user("ops01", List.of("ghost")));

        assertThatThrownBy(() -> service.sync(users))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void skipsWhenUserMissing() {
        when(adminUserDao.selectOne(any())).thenReturn(null);

        InitialUserRoleBindingService.SyncResult result = service.sync(List.of(
                user("ops01", List.of("sys_admin"))));

        verify(roleBindingDao, never()).insert(any(RoleBindingEntity.class));
        assertThat(result.bound()).isZero();
    }

    @Test
    void ignoresUsersWithoutRoleCodes() {
        InitialUserRoleBindingService.SyncResult result = service.sync(List.of(
                user("ops01", List.of())));

        verify(roleDao, never()).selectOne(any());
        assertThat(result.bound()).isZero();
    }

    private InitialUser user(String loginName, List<String> roleCodes) {
        return new InitialUser(loginName, null, null, null, "Init12345", "PLATFORM", roleCodes);
    }

    private AdminUserEntity adminUser(String id, String loginName) {
        AdminUserEntity entity = new AdminUserEntity();
        entity.setAdminUserId(id);
        entity.setLoginName(loginName);
        entity.setSecurityRealm("PLATFORM");
        return entity;
    }

    private RoleEntity role(String roleCode, String roleId) {
        RoleEntity role = new RoleEntity();
        role.setRoleId(roleId);
        role.setRoleCode(roleCode);
        role.setOwnerType("PLATFORM");
        role.setSecurityRealm("PLATFORM");
        return role;
    }
}
