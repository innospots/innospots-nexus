package com.innospots.nexus.portal.registration.domain;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.portal.registration.domain.request.TenantRegisterRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TenantRegistrationDomainContractsTest {

    @Test
    void tenantRegisterRequestIsARecord() {
        assertThat(TenantRegisterRequest.class.isRecord()).isTrue();
        assertThat(Arrays.stream(TenantRegisterRequest.class.getRecordComponents())
                .map(RecordComponent::getName))
                .containsExactly(
                        "userName", "displayName", "email", "mobile",
                        "region", "timeZone", "language", "encryptedPassword");
    }
}
