package com.innospots.nexus.base.domain.organization;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationContractsTest {

    @Test
    void definesOrganizationProfile() {
        OrganizationInfo organization = new OrganizationInfo(1L, "Innospots", "innospots", "zh-CN", "CNY", "logo", BasicStatus.ENABLED);

        assertThat(organization.organizationId()).isEqualTo(1L);
        assertThat(organization.defaultLocale()).isEqualTo("zh-CN");
        assertThat(organization.status()).isEqualTo(BasicStatus.ENABLED);
    }
}
