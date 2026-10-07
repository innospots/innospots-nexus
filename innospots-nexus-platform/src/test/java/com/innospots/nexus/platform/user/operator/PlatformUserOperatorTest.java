package com.innospots.nexus.platform.user.operator;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformUserOperatorTest {

    @Test
    void ensureLoginNameAvailableRejectsDuplicate() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        PlatformUserOperator operator = new PlatformUserOperator(userDao);

        assertThatThrownBy(() -> operator.ensureLoginNameAvailable("ops.alice"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformUserStatusCode.LOGIN_NAME_DUPLICATED.fullCode());
    }

    @Test
    void ensureEmailAvailableRejectsDuplicateIgnoringCase() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        PlatformUserOperator operator = new PlatformUserOperator(userDao);

        assertThatThrownBy(() -> operator.ensureEmailAvailable("Alice@Example.com", null))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformUserStatusCode.EMAIL_DUPLICATED.fullCode());
    }

    @Test
    void updateStatusPersistsTargetStatus() {
        PlatformUserDao userDao = mock(PlatformUserDao.class);
        PlatformUserEntity entity = new PlatformUserEntity();
        entity.setPlatformUserId("pus_test");
        entity.setStatus(PlatformUserStatus.ACTIVE.name());
        when(userDao.selectById("pus_test")).thenReturn(entity);
        PlatformUserOperator operator = new PlatformUserOperator(userDao);

        operator.updateStatus(entity, PlatformUserStatus.PENDING_ACTIVATION);

        assertThat(entity.getStatus()).isEqualTo(PlatformUserStatus.PENDING_ACTIVATION.name());
        verify(userDao).updateById(entity);
    }

    @Test
    void requireByIdThrowsWhenMissing() {
        PlatformUserOperator operator = new PlatformUserOperator(mock(PlatformUserDao.class));

        assertThatThrownBy(() -> operator.requireById("missing"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformUserStatusCode.PLATFORM_USER_NOT_FOUND.fullCode());
    }

    @Test
    void activatePendingApprovalDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(PlatformUserOperator.class
                .getDeclaredMethod("activatePendingApproval", PlatformUserEntity.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(PlatformUserOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
