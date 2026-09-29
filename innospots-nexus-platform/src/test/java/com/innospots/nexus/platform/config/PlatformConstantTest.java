package com.innospots.nexus.platform.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConstantTest {

    @Test
    void platformPathsMatchOperationalEndpoints() {
        assertThat(PlatformConstant.API_PREFIX).isEqualTo("/platform");
        assertThat(PlatformConstant.AUTH_PATH).isEqualTo("/platform/auth");
        assertThat(PlatformConstant.TENANTS_PATH).isEqualTo("/platform/tenants");
        assertThat(PlatformConstant.USERS_PATH).isEqualTo("/platform/users");
        assertThat(PlatformConstant.apiPath("/auth")).isEqualTo(PlatformConstant.AUTH_PATH);
        assertThat(PlatformConstant.apiPath("tenants")).isEqualTo(PlatformConstant.TENANTS_PATH);
    }
}
