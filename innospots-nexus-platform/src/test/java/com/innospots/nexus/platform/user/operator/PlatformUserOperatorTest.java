package com.innospots.nexus.platform.user.operator;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.credential.api.PasswordDecryptor;
import com.innospots.nexus.core.persistence.id.DbPrimaryGenerator;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.dao.PlatformUserPasswordDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserPasswordEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformUserOperatorTest {

    @Test
    void createWithPasswordPersistsUserThenCredential() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        PlatformUserPasswordDao passwordDao = mock(PlatformUserPasswordDao.class);
        PasswordDecryptor decryptor = mock(PasswordDecryptor.class);
        DbPrimaryGenerator generator = new DbPrimaryGenerator();
        doAnswer(invocation -> {
            PlatformUserEntity entity = invocation.getArgument(0);
            entity.setPlatformUserId(generator.nextUUID(entity));
            return 1;
        }).when(userDao).insert(any(PlatformUserEntity.class));
        doAnswer(invocation -> {
            PlatformUserPasswordEntity entity = invocation.getArgument(0);
            entity.setCredentialId(generator.nextUUID(entity));
            return 1;
        }).when(passwordDao).insert(any(PlatformUserPasswordEntity.class));
        when(decryptor.decrypt(eq("front-encrypted-password"))).thenReturn("raw-secret");

        PlatformUserVo created = new PlatformUserOperator(userDao, passwordDao, decryptor)
                .createWithPassword(new PlatformUserCreateRequest(
                        "ops.alice",
                        "Alice",
                        "alice@innospots.com",
                        "13800000001",
                        "E001",
                        "front-encrypted-password"));

        assertThat(created.platformUserId()).startsWith("pus");
        assertThat(created.platformUserId()).hasSize(29);
        assertThat(created.loginName()).isEqualTo("ops.alice");
        assertThat(created.status()).isEqualTo(PlatformUserStatus.ACTIVE.name());
        verify(decryptor).decrypt("front-encrypted-password");

        ArgumentCaptor<PlatformUserPasswordEntity> credentialCaptor = forClass(PlatformUserPasswordEntity.class);
        verify(passwordDao).insert(credentialCaptor.capture());
        PlatformUserPasswordEntity credential = credentialCaptor.getValue();
        assertThat(credential.getCredentialId()).startsWith("ppc");
        assertThat(credential.getPlatformUserId()).isEqualTo(created.platformUserId());
        assertThat(CryptoUtils.matchesPassword("raw-secret", credential.getPasswordHash())).isTrue();
    }

    @Test
    void createWithPasswordRejectsMissingLoginName() {
        PlatformUserOperator operator = new PlatformUserOperator(
                mock(PlatformUserDao.class),
                mock(PlatformUserPasswordDao.class),
                mock(PasswordDecryptor.class));

        assertThatThrownBy(() -> operator.createWithPassword(new PlatformUserCreateRequest(
                " ", "Alice", null, null, null, "encrypted")))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void createDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(PlatformUserOperator.class
                .getDeclaredMethod("createWithPassword", PlatformUserCreateRequest.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(PlatformUserOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
