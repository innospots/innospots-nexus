package com.innospots.nexus.platform.organization.operator;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.organization.dao.TenantDao;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantStatus;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TenantOperatorTest {

    @Test
    void insertPersistsTenantOnly() {
        TenantDao tenantDao = mock(TenantDao.class);
        doAnswer(invocation -> {
            TenantEntity stored = invocation.getArgument(0);
            stored.setTenantId("tnt01HZY8J6Y3D6S4V7N9X2M5Q8");
            return 1;
        }).when(tenantDao).insert(any(TenantEntity.class));

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantName("Acme");
        tenant.setTenantCode("acme");
        tenant.setTenantType(TenantType.TEAM.name());

        TenantOperator operator = new TenantOperator(tenantDao);
        operator.insert(tenant);

        assertThat(tenant.getTenantId()).isEqualTo("tnt01HZY8J6Y3D6S4V7N9X2M5Q8");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE.name());
        assertThat(tenant.getTenantType()).isEqualTo(TenantType.TEAM.name());
        verify(tenantDao).insert(tenant);
    }

    @Test
    void insertRejectsInvalidTenantType() {
        TenantOperator operator = new TenantOperator(mock(TenantDao.class));
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantName("Acme");
        tenant.setTenantCode("acme");
        tenant.setTenantType("INVALID");

        assertThatThrownBy(() -> operator.insert(tenant))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void insertDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(TenantOperator.class
                .getDeclaredMethod("insert", TenantEntity.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(TenantOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
