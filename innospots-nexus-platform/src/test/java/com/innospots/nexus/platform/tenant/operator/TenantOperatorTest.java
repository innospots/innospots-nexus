package com.innospots.nexus.platform.tenant.operator;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.enterprise.dao.EnterpriseDao;
import com.innospots.nexus.platform.enterprise.domain.entity.EnterpriseEntity;
import com.innospots.nexus.platform.tenant.dao.TenantDao;
import com.innospots.nexus.platform.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.platform.tenant.domain.enums.TenantStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TenantOperatorTest {

    @Test
    void createPersistsTenantThenEnterpriseWithSharedTenantId() {
        TenantDao tenantDao = mock(TenantDao.class);
        EnterpriseDao enterpriseDao = mock(EnterpriseDao.class);
        doAnswer(invocation -> {
            TenantEntity stored = invocation.getArgument(0);
            stored.setTenantId("tnt01HZY8J6Y3D6S4V7N9X2M5Q8");
            return 1;
        }).when(tenantDao).insert(any(TenantEntity.class));

        TenantEntity tenant = new TenantEntity();
        tenant.setTenantName("Acme");
        tenant.setTenantCode("acme");
        EnterpriseEntity enterprise = new EnterpriseEntity();
        enterprise.setLegalName("Acme Ltd");

        TenantEntity created = new TenantOperator(tenantDao, enterpriseDao).create(tenant, enterprise);

        assertThat(created.getTenantId()).isEqualTo("tnt01HZY8J6Y3D6S4V7N9X2M5Q8");
        assertThat(created.getStatus()).isEqualTo(TenantStatus.ACTIVE.name());
        assertThat(enterprise.getTenantId()).isEqualTo("tnt01HZY8J6Y3D6S4V7N9X2M5Q8");
        verify(tenantDao).insert(tenant);
        verify(enterpriseDao).insert(enterprise);
    }

    @Test
    void createRejectsMissingLegalName() {
        TenantOperator operator = new TenantOperator(mock(TenantDao.class), mock(EnterpriseDao.class));
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantName("Acme");
        tenant.setTenantCode("acme");

        assertThatThrownBy(() -> operator.create(tenant, new EnterpriseEntity()))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void createDeclaresTransactionalBoundaryAndLogger() throws Exception {
        assertThat(TenantOperator.class
                .getDeclaredMethod("create", TenantEntity.class, EnterpriseEntity.class)
                .getAnnotation(Transactional.class)).isNotNull();
        assertThat(TenantOperator.class.getDeclaredField("log").getType()).isEqualTo(Logger.class);
    }
}
