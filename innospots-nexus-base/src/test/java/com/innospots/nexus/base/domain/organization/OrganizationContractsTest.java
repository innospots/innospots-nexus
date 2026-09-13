package com.innospots.nexus.base.domain.organization;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationContractsTest {

    @Test
    void definesTenantBusinessProfile() {
        OrganizationSnapshot organization = new OrganizationSnapshot(
                "tnt01", "innospots", "Innospots", "zh-CN", "CNY", "logo", BasicStatus.ENABLED);

        assertThat(organization.tenantId()).isEqualTo("tnt01");
        assertThat(organization.defaultLocale()).isEqualTo("zh-CN");
        assertThat(organization.status()).isEqualTo(BasicStatus.ENABLED);
    }
}
