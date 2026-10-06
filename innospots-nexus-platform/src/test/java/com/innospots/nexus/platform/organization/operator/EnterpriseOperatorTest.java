package com.innospots.nexus.platform.organization.operator;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.organization.dao.EnterpriseDao;
import com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnterpriseOperatorTest {

    @Test
    void insertRejectsDuplicateProfileForTenant() {
        EnterpriseDao enterpriseDao = mock(EnterpriseDao.class);
        EnterpriseEntity existing = new EnterpriseEntity();
        existing.setTenantId("tnt01");
        when(enterpriseDao.selectOne(any())).thenReturn(existing);

        EnterpriseEntity candidate = new EnterpriseEntity();
        candidate.setTenantId("tnt01");
        candidate.setLegalName("Acme Ltd");

        EnterpriseOperator operator = new EnterpriseOperator(enterpriseDao);
        assertThatThrownBy(() -> operator.insert(candidate))
                .isInstanceOf(NexusException.class);
    }
}
