package com.innospots.nexus.console.bootstrap.service;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.bootstrap.domain.config.InitialUserManifest.InitialUser;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link InitialUserSyncService} 用户创建语义测试（启动引导第 4 阶段）。
 *
 * <p>本阶段只负责用户建档与初始密码；角色绑定由
 * {@link InitialUserRoleBindingServiceTest} 覆盖。</p>
 */
class InitialUserSyncServiceTest {

    private final AdminUserDao adminUserDao = mock(AdminUserDao.class);
    private final CredentialService credentialService = mock(CredentialService.class);

    private final InitialUserSyncService service =
            new InitialUserSyncService(adminUserDao, credentialService);

    @Test
    void createsUserWithInitialPassword() {
        when(adminUserDao.selectOne(any())).thenReturn(null);

        InitialUserSyncService.SyncResult result = service.sync(List.of(
                new InitialUser(
                        "ops01", "运营一号", "ops01@demo.com", "13800000000",
                        "Init12345", "PLATFORM", List.of("sys_admin"))));

        ArgumentCaptor<AdminUserEntity> captor = ArgumentCaptor.forClass(AdminUserEntity.class);
        verify(adminUserDao).insert(captor.capture());
        AdminUserEntity user = captor.getValue();
        assertThat(user.getLoginName()).isEqualTo("ops01");
        assertThat(user.getDisplayName()).isEqualTo("运营一号");
        assertThat(user.getSecurityRealm()).isEqualTo(SecurityRealm.PLATFORM.name());
        assertThat(user.getStatus()).isEqualTo(AdminUserStatus.ACTIVE.name());

        // adminUserId 由 MyBatis 主键生成器填充，单元测试中 DAO 为 mock 不会赋值；
        // 这里只验证凭证写入使用了同一实体的主键。
        verify(credentialService).enrollPassword(
                eq(SecurityRealm.PLATFORM), eq(user.getAdminUserId()), eq("Init12345"));
        assertThat(result.created()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
    }

    @Test
    void fallsBackToLoginNameAsDisplayName() {
        when(adminUserDao.selectOne(any())).thenReturn(null);

        service.sync(List.of(new InitialUser(
                "ops01", "  ", null, null, "Init12345", "PLATFORM", List.of())));

        ArgumentCaptor<AdminUserEntity> captor = ArgumentCaptor.forClass(AdminUserEntity.class);
        verify(adminUserDao).insert(captor.capture());
        assertThat(captor.getValue().getDisplayName()).isEqualTo("ops01");
    }

    @Test
    void skipsExistingUserWithoutResettingPassword() {
        AdminUserEntity existing = new AdminUserEntity();
        existing.setAdminUserId("ADM00000000001");
        existing.setLoginName("ops01");
        when(adminUserDao.selectOne(any())).thenReturn(existing);

        InitialUserSyncService.SyncResult result = service.sync(List.of(
                new InitialUser(
                        "ops01", null, null, null, "Changed123", "PLATFORM", List.of("sys_admin"))));

        verify(adminUserDao, never()).insert(any(AdminUserEntity.class));
        verify(credentialService, never()).enrollPassword(
                any(SecurityRealm.class), anyString(), anyString());
        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
    }

    @Test
    void failsWhenPasswordMissing() {
        when(adminUserDao.selectOne(any())).thenReturn(null);

        List<InitialUser> users = List.of(
                new InitialUser("ops01", null, null, null, "  ", "PLATFORM", List.of()));

        assertThatThrownBy(() -> service.sync(users))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("password");
    }

    @Test
    void rejectsUnknownSecurityRealm() {
        List<InitialUser> users = List.of(
                new InitialUser("ops01", null, null, null, "Init12345", "GLOBAL", List.of()));

        assertThatThrownBy(() -> service.sync(users))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("GLOBAL");
    }

    @Test
    void treatsEmptyListAsNoop() {
        InitialUserSyncService.SyncResult result = service.sync(List.of());

        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isZero();
        verify(adminUserDao, never()).insert(any(AdminUserEntity.class));
    }
}
