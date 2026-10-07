package com.innospots.nexus.platform.auth.status;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformAuthStatusCodeTest {

    @Test
    void platformAuthStatusCodesUsePafModulePrefix() {
        assertThat(PlatformAuthStatusCode.PASSWORD_RESET_VERIFICATION_INVALID.fullCode()).startsWith("PAF");
        assertThat(PlatformAuthStatusCode.PASSWORD_RESET_VERIFICATION_INVALID.httpStatusCode()).isEqualTo(400);
    }
}
