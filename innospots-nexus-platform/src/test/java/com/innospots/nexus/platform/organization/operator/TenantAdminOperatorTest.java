package com.innospots.nexus.platform.organization.operator;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.organization.dao.TenantAdminDao;
import com.innospots.nexus.platform.organization.domain.entity.TenantAdminEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TenantAdminOperatorTest {

    @Test
    void insertRejectsDuplicateEmailForTenant() {
        TenantAdminDao dao = mock(TenantAdminDao.class);
        TenantAdminOperator operator = new TenantAdminOperator(dao);

        TenantAdminEntity existing = new TenantAdminEntity();
        existing.setTenantId("tnt01");
        existing.setEmail("admin@example.com");
        when(dao.selectOne(any())).thenReturn(existing);

        TenantAdminEntity candidate = new TenantAdminEntity();
        candidate.setTenantId("tnt01");
        candidate.setEmail("admin@example.com");
        candidate.setAdminRole(TenantAdminRole.ADMINISTRATOR.name());

        assertThatThrownBy(() -> operator.insert(candidate))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void requireAdminRoleRejectsUnknownValue() {
        assertThatThrownBy(() -> TenantAdminOperator.requireAdminRole("SUPERUSER"))
                .isInstanceOf(NexusException.class);
    }
}
