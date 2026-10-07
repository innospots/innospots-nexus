package com.innospots.nexus.platform.access.support;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformRegistrationContactSupportTest {

    @Test
    void resolveVerificationContactPrefersEmailWhenBothProvided() {
        assertThat(PlatformRegistrationContactSupport.resolveVerificationContact(
                        "User@Example.com", "13800000001"))
                .isEqualTo("User@Example.com");
    }

    @Test
    void resolveVerificationContactForOtpRequestUsesSameRuleAsSubmit() {
        assertThat(PlatformRegistrationContactSupport.resolveVerificationContactForOtpRequest(
                        "a@b.com",
                        "13800000001",
                        "ignored-contact"))
                .isEqualTo("a@b.com");
        assertThat(PlatformRegistrationContactSupport.resolveVerificationContactForOtpRequest(
                        null,
                        null,
                        "13800000002"))
                .isEqualTo("13800000002");
    }

    @Test
    void resolveOpenRegistrationLoginNameRequiresLoginNameForMobileOnly() {
        assertThatThrownBy(() -> PlatformRegistrationContactSupport.resolveOpenRegistrationLoginName(
                        null,
                        null,
                        "13800000001"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformAccessStatusCode.ACCESS_LOGIN_NAME_REQUIRED.fullCode());
    }

    @Test
    void resolveOpenRegistrationLoginNameDerivesFromEmail() {
        assertThat(PlatformRegistrationContactSupport.resolveOpenRegistrationLoginName(
                        null,
                        "Alice@Example.com",
                        null))
                .isEqualTo("alice");
    }
}
