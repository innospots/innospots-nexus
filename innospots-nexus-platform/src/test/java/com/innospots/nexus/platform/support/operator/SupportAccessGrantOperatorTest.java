package com.innospots.nexus.platform.support.operator;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.support.dao.SupportAccessGrantDao;
import com.innospots.nexus.platform.support.domain.entity.SupportAccessGrantEntity;
import com.innospots.nexus.platform.support.domain.enums.SupportAccessStatus;
import com.innospots.nexus.platform.support.domain.request.SupportAccessGrantCreateRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SupportAccessGrantOperatorTest {

    @Test
    void createPersistsPendingGrant() {
        SupportAccessGrantDao dao = mock(SupportAccessGrantDao.class);
        doAnswer(invocation -> {
            SupportAccessGrantEntity stored = invocation.getArgument(0);
            stored.setGrantId("sag01HZY8J6Y3D6S4V7N9X2M5Q8");
            return 1;
        }).when(dao).insert(any(SupportAccessGrantEntity.class));

        LocalDateTime expireAt = LocalDateTime.of(2026, 9, 1, 8, 0);
        SupportAccessGrantEntity created = new SupportAccessGrantOperator(dao).create(
                new SupportAccessGrantCreateRequest("tnt-1", "pus-1", "incident review", expireAt));

        assertThat(created.getGrantId()).isEqualTo("sag01HZY8J6Y3D6S4V7N9X2M5Q8");
        assertThat(created.getTenantId()).isEqualTo("tnt-1");
        assertThat(created.getPlatformUserId()).isEqualTo("pus-1");
        assertThat(created.getReason()).isEqualTo("incident review");
        assertThat(created.getExpireAt()).isEqualTo(expireAt);
        assertThat(created.getStatus()).isEqualTo(SupportAccessStatus.PENDING.name());
        verify(dao).insert(created);
    }

    @Test
    void createRejectsMissingReason() {
        SupportAccessGrantOperator operator = new SupportAccessGrantOperator(mock(SupportAccessGrantDao.class));

        assertThatThrownBy(() -> operator.create(
                new SupportAccessGrantCreateRequest("tnt-1", "pus-1", "  ", LocalDateTime.now())))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void createDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(SupportAccessGrantOperator.class
                .getDeclaredMethod("create", SupportAccessGrantCreateRequest.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(SupportAccessGrantOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
