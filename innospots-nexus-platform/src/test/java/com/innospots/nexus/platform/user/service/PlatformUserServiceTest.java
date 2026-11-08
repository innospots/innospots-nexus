package com.innospots.nexus.console.user.admin.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.core.persistence.id.DbPrimaryGenerator;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserCreateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPasswordResetRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserStatusUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.vo.AdminUserVo;
import com.innospots.nexus.console.user.admin.operator.AdminUserOperator;
import com.innospots.nexus.console.user.admin.status.AdminUserStatusCode;
import com.innospots.nexus.console.user.admin.support.AdminUserRoleProvisioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    @Test
    void createUserPersistsUserThenCredentialAndRoles() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        CredentialService credentialService = mock(CredentialService.class);
        PasswordDecryptor decryptor = mock(PasswordDecryptor.class);
        AdminUserRoleProvisioner roleProvisioner = mock(AdminUserRoleProvisioner.class);
        DbPrimaryGenerator generator = new DbPrimaryGenerator();
        when(userDao.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            AdminUserEntity entity = invocation.getArgument(0);
            entity.setAdminUserId(generator.nextUUID(entity));
            return 1;
        }).when(userDao).insert(any(AdminUserEntity.class));
        when(decryptor.decrypt(eq("front-encrypted-password"))).thenReturn("raw-secret");

        AdminUserService service = new AdminUserService(
                new AdminUserOperator(userDao),
                credentialService,
                decryptor,
                roleProvisioner);

        AdminUserVo created = service.createUser(new AdminUserCreateRequest(
                "ops.alice",
                "Alice",
                "alice@innospots.com",
                "13800000001",
                "ops,viewer",
                "front-encrypted-password",
                null));

        assertThat(created.adminUserId()).startsWith("ADM");
        assertThat(created.loginName()).isEqualTo("ops.alice");
        assertThat(created.status()).isEqualTo(AdminUserStatus.ACTIVE.name());
        verify(decryptor).decrypt("front-encrypted-password");
        verify(credentialService).enrollPassword(
                eq(SecurityRealm.PLATFORM),
                eq(created.adminUserId()),
                eq("raw-secret"));
        verify(roleProvisioner).assignDefaultRolesIfPresent(created.adminUserId(), "ops,viewer");
    }

    @Test
    void createUserRejectsMissingLoginName() {
        AdminUserService service = serviceWithMocks(mock(AdminUserDao.class));

        assertThatThrownBy(() -> service.createUser(new AdminUserCreateRequest(
                " ", "Alice", null, null, null, "encrypted", null)))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void createUserRejectsDuplicateLoginName() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        AdminUserService service = serviceWithMocks(userDao);

        assertThatThrownBy(() -> service.createUser(new AdminUserCreateRequest(
                "ops.alice",
                "Alice",
                null,
                null,
                null,
                "encrypted",
                null)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(AdminUserStatusCode.LOGIN_NAME_DUPLICATED.fullCode());
    }

    @Test
    void resetUserPasswordDecryptsAndResetsCredential() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        CredentialService credentialService = mock(CredentialService.class);
        PasswordDecryptor decryptor = mock(PasswordDecryptor.class);
        AdminUserEntity entity = new AdminUserEntity();
        entity.setAdminUserId("pus-1");
        when(userDao.selectById("pus-1")).thenReturn(entity);
        when(decryptor.decrypt("cipher")).thenReturn("NewPass1");

        AdminUserService service = new AdminUserService(
                new AdminUserOperator(userDao),
                credentialService,
                decryptor,
                mock(AdminUserRoleProvisioner.class));

        service.resetUserPassword("pus-1", new AdminUserPasswordResetRequest("cipher"));

        verify(decryptor).decrypt("cipher");
        verify(credentialService).resetPassword(SecurityRealm.PLATFORM, "pus-1", "NewPass1");
    }

    @Test
    void updateUserStatusRejectsNonAdminStatuses() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        AdminUserEntity entity = new AdminUserEntity();
        entity.setAdminUserId("pus-1");
        entity.setStatus(AdminUserStatus.ACTIVE.name());
        when(userDao.selectById("pus-1")).thenReturn(entity);

        AdminUserService service = serviceWithMocks(userDao);

        assertThatThrownBy(() -> service.updateUserStatus(
                        "pus-1",
                        new AdminUserStatusUpdateRequest(AdminUserStatus.LOCKED)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(AdminUserStatusCode.STATUS_UPDATE_NOT_ALLOWED.fullCode());
    }

    private static AdminUserService serviceWithMocks(AdminUserDao userDao) {
        return new AdminUserService(
                new AdminUserOperator(userDao),
                mock(CredentialService.class),
                mock(PasswordDecryptor.class),
                mock(AdminUserRoleProvisioner.class));
    }
}
