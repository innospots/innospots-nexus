package com.innospots.nexus.platform.user.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.core.persistence.id.DbPrimaryGenerator;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserStatusUpdateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformUserServiceTest {

    @Test
    void createUserPersistsUserThenCredentialAndRoles() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        CredentialService credentialService = mock(CredentialService.class);
        PasswordDecryptor decryptor = mock(PasswordDecryptor.class);
        PlatformUserRoleProvisioner roleProvisioner = mock(PlatformUserRoleProvisioner.class);
        DbPrimaryGenerator generator = new DbPrimaryGenerator();
        when(userDao.selectCount(any())).thenReturn(0L);
        doAnswer(invocation -> {
            PlatformUserEntity entity = invocation.getArgument(0);
            entity.setPlatformUserId(generator.nextUUID(entity));
            return 1;
        }).when(userDao).insert(any(PlatformUserEntity.class));
        when(decryptor.decrypt(eq("front-encrypted-password"))).thenReturn("raw-secret");

        PlatformUserService service = new PlatformUserService(
                new PlatformUserOperator(userDao),
                credentialService,
                decryptor,
                roleProvisioner);

        PlatformUserVo created = service.createUser(new PlatformUserCreateRequest(
                "ops.alice",
                "Alice",
                "alice@innospots.com",
                "13800000001",
                "E001",
                "ops,viewer",
                "front-encrypted-password"));

        assertThat(created.platformUserId()).startsWith("pus");
        assertThat(created.loginName()).isEqualTo("ops.alice");
        assertThat(created.status()).isEqualTo(PlatformUserStatus.ACTIVE.name());
        verify(decryptor).decrypt("front-encrypted-password");
        verify(credentialService).enrollPassword(
                eq(SecurityRealm.PLATFORM),
                eq(created.platformUserId()),
                eq("raw-secret"));
        verify(roleProvisioner).assignDefaultRolesIfPresent(created.platformUserId(), "ops,viewer");
    }

    @Test
    void createUserRejectsMissingLoginName() {
        PlatformUserService service = serviceWithMocks(mock(PlatformUserDao.class));

        assertThatThrownBy(() -> service.createUser(new PlatformUserCreateRequest(
                " ", "Alice", null, null, null, null, "encrypted")))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void createUserRejectsDuplicateLoginName() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        PlatformUserService service = serviceWithMocks(userDao);

        assertThatThrownBy(() -> service.createUser(new PlatformUserCreateRequest(
                "ops.alice",
                "Alice",
                null,
                null,
                null,
                null,
                "encrypted")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformUserStatusCode.LOGIN_NAME_DUPLICATED.fullCode());
    }

    @Test
    void updateUserStatusRejectsNonAdminStatuses() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        PlatformUserEntity entity = new PlatformUserEntity();
        entity.setPlatformUserId("pus-1");
        entity.setStatus(PlatformUserStatus.ACTIVE.name());
        when(userDao.selectById("pus-1")).thenReturn(entity);

        PlatformUserService service = serviceWithMocks(userDao);

        assertThatThrownBy(() -> service.updateUserStatus(
                        "pus-1",
                        new PlatformUserStatusUpdateRequest(PlatformUserStatus.LOCKED)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformUserStatusCode.STATUS_UPDATE_NOT_ALLOWED.fullCode());
    }

    private static PlatformUserService serviceWithMocks(PlatformUserDao userDao) {
        return new PlatformUserService(
                new PlatformUserOperator(userDao),
                mock(CredentialService.class),
                mock(PasswordDecryptor.class),
                mock(PlatformUserRoleProvisioner.class));
    }
}
