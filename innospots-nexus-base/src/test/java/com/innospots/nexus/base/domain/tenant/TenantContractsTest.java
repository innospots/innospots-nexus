package com.innospots.nexus.base.domain.tenant;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantContractsTest {

    @Test
    void definesTenantProfile() {
        TenantSnapshot tenant = new TenantSnapshot("tnt01", "acme", "Acme", BasicStatus.ENABLED);

        assertThat(tenant.tenantId()).isEqualTo("tnt01");
        assertThat(tenant.tenantCode()).isEqualTo("acme");
        assertThat(tenant.status()).isEqualTo(BasicStatus.ENABLED);
    }
}
