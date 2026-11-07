package com.innospots.nexus.console.user.admin.status;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminUserStatusCodeTest {

    @Test
    void platformUserStatusCodesUsePluModulePrefix() {
        assertThat(AdminUserStatusCode.PLATFORM_USER_NOT_FOUND.fullCode()).startsWith("PLU");
        assertThat(AdminUserStatusCode.LOGIN_NAME_DUPLICATED.httpStatusCode()).isEqualTo(409);
    }
}
