package com.innospots.nexus.platform.user.status;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformUserStatusCodeTest {

    @Test
    void platformUserStatusCodesUsePluModulePrefix() {
        assertThat(PlatformUserStatusCode.PLATFORM_USER_NOT_FOUND.fullCode()).startsWith("PLU");
        assertThat(PlatformUserStatusCode.LOGIN_NAME_DUPLICATED.httpStatusCode()).isEqualTo(409);
    }
}
