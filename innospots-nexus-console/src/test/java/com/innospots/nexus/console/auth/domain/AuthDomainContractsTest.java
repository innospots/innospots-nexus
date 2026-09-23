package com.innospots.nexus.console.auth.domain;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.SelectProjectRequest;
import com.innospots.nexus.console.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.console.auth.domain.request.SelectWorkspaceRequest;
import com.innospots.nexus.console.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDomainContractsTest {

    @Test
    void authRequestsAndTokenAreRecords() {
        assertRecordComponents(AuthLoginRequest.class,
                "login", "encryptedPassword", "captchaClientKey", "captchaCode");
        assertRecordComponents(TenantRegisterRequest.class,
                "userName", "displayName", "email", "mobile", "region", "timeZone", "language", "encryptedPassword");
        assertRecordComponents(SelectTenantRequest.class, "tenantId");
        assertRecordComponents(SelectWorkspaceRequest.class, "tenantId", "workspaceId");
        assertRecordComponents(SelectProjectRequest.class, "tenantId", "workspaceId", "projectId");
        assertRecordComponents(AuthTokenVo.class,
                "realm", "tokenType", "accessToken", "refreshToken",
                "tenantId", "tenantMemberId", "workspaceId", "projectId");
        assertThat(SecurityRealm.values()).containsExactly(SecurityRealm.PLATFORM, SecurityRealm.TENANT);
    }

    private static void assertRecordComponents(Class<?> recordType, String... names) {
        assertThat(recordType.isRecord()).isTrue();
        assertThat(Arrays.stream(recordType.getRecordComponents()).map(RecordComponent::getName))
                .containsExactly(names);
    }
}
