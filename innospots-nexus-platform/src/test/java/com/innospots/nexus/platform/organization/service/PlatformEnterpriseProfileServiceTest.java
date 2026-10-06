package com.innospots.nexus.platform.organization.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;
import com.innospots.nexus.platform.organization.domain.request.EnterpriseProfileUpsertRequest;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformEnterpriseProfileServiceTest {

    @Test
    void upsertRejectsNonEnterpriseTenantType() {
        TenantOperator tenantOperator = mock(TenantOperator.class);
        TenantEntity team = new TenantEntity();
        team.setTenantId("tnt01");
        team.setTenantType(TenantType.TEAM.name());
        when(tenantOperator.requireById("tnt01")).thenReturn(team);

        PlatformEnterpriseProfileService service = new PlatformEnterpriseProfileService(
                tenantOperator,
                mock(EnterpriseOperator.class));

        EnterpriseProfileUpsertRequest request = new EnterpriseProfileUpsertRequest(
                "Acme Ltd",
                null,
                null,
                null,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> service.upsertEnterpriseProfile("tnt01", request))
                .isInstanceOf(NexusException.class);
    }
}
