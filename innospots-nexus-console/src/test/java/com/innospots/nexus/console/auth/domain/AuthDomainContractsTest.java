package com.innospots.nexus.console.auth.domain;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDomainContractsTest {

    @Test
    void authRequestsAndTokenAreRecords() {
        assertRecordComponents(AuthLoginRequest.class,
                "login", "encryptedPassword", "captchaClientKey", "captchaCode");
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
