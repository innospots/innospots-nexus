package com.innospots.nexus.portal.auth.domain;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.portal.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.portal.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.portal.scope.domain.request.SelectProjectRequest;
import com.innospots.nexus.portal.scope.domain.request.SelectWorkspaceRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantAuthDomainContractsTest {

    @Test
    void tenantAuthRequestsAreRecords() {
        assertRecordComponents(TenantRegisterRequest.class,
                "userName", "displayName", "email", "mobile", "region", "timeZone", "language", "encryptedPassword");
        assertRecordComponents(SelectTenantRequest.class, "tenantId");
        assertRecordComponents(SelectWorkspaceRequest.class, "tenantId", "workspaceId");
        assertRecordComponents(SelectProjectRequest.class, "tenantId", "workspaceId", "projectId");
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly(names);
    }
}
