package com.innospots.nexus.portal.auth.domain;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.portal.auth.domain.request.SelectTenantRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAuthDomainContractsTest {

    @Test
    void tenantAuthRequestsAreRecords() {
        assertRecordComponents(SelectTenantRequest.class, "tenantId");
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly(names);
    }
}
