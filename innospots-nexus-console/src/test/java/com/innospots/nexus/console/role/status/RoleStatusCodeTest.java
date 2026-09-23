package com.innospots.nexus.console.role.status;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleStatusCodeTest {

    @Test
    void roleStatusCodesUseRolModulePrefix() {
        assertThat(RoleStatusCode.ROLE_NOT_FOUND.module()).isEqualTo("ROL");
        assertThat(RoleStatusCode.ROLE_NOT_FOUND.fullCode()).startsWith("ROL");
        assertThat(RoleStatusCode.ROLE_CODE_DUPLICATED.httpStatusCode()).isEqualTo(409);
    }
}
