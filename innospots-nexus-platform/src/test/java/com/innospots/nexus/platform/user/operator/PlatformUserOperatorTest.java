package com.innospots.nexus.console.user.admin.operator;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;
import com.innospots.nexus.console.user.admin.status.AdminUserStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserOperatorTest {

    @Test
    void ensureLoginNameAvailableRejectsDuplicate() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        AdminUserOperator operator = new AdminUserOperator(userDao);

        assertThatThrownBy(() -> operator.ensureLoginNameAvailable("ops.alice"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(AdminUserStatusCode.LOGIN_NAME_DUPLICATED.fullCode());
    }

    @Test
    void ensureEmailAvailableRejectsDuplicateIgnoringCase() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        when(userDao.selectCount(any())).thenReturn(1L);
        AdminUserOperator operator = new AdminUserOperator(userDao);

        assertThatThrownBy(() -> operator.ensureEmailAvailable("Alice@Example.com", null))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(AdminUserStatusCode.EMAIL_DUPLICATED.fullCode());
    }

    @Test
    void updateStatusPersistsTargetStatus() {
        AdminUserDao userDao = mock(AdminUserDao.class);
        AdminUserEntity entity = new AdminUserEntity();
        entity.setPlatformUserId("pus_test");
        entity.setStatus(AdminUserStatus.ACTIVE.name());
        when(userDao.selectById("pus_test")).thenReturn(entity);
        AdminUserOperator operator = new AdminUserOperator(userDao);

        operator.updateStatus(entity, AdminUserStatus.PENDING_ACTIVATION);

        assertThat(entity.getStatus()).isEqualTo(AdminUserStatus.PENDING_ACTIVATION.name());
        verify(userDao).updateById(entity);
    }

    @Test
    void requireByIdThrowsWhenMissing() {
        AdminUserOperator operator = new AdminUserOperator(mock(AdminUserDao.class));

        assertThatThrownBy(() -> operator.requireById("missing"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(AdminUserStatusCode.PLATFORM_USER_NOT_FOUND.fullCode());
    }

    @Test
    void activatePendingApprovalDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(AdminUserOperator.class
                .getDeclaredMethod("activatePendingApproval", AdminUserEntity.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(AdminUserOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
