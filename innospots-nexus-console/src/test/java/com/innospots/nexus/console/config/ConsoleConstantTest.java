package com.innospots.nexus.console.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleConstantTest {

    @Test
    void platformPathsMatchOperationalEndpoints() {
        assertThat(ConsoleConstant.PLATFORM_API_PREFIX).isEqualTo("/platform");
        assertThat(ConsoleConstant.PLATFORM_AUTH_PATH).isEqualTo("/platform/auth");
        assertThat(ConsoleConstant.PLATFORM_TENANTS_PATH).isEqualTo("/platform/tenants");
        assertThat(ConsoleConstant.PLATFORM_USERS_PATH).isEqualTo("/platform/users");
        assertThat(ConsoleConstant.platformPath("/auth")).isEqualTo(ConsoleConstant.PLATFORM_AUTH_PATH);
        assertThat(ConsoleConstant.platformPath("tenants")).isEqualTo(ConsoleConstant.PLATFORM_TENANTS_PATH);
    }
}
