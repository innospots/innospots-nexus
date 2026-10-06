package com.innospots.nexus.platform.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConstantTest {

    @Test
    void platformPathsMatchOperationalEndpoints() {
        assertThat(PlatformConstant.API_PREFIX).isEqualTo("/api/platform");
        assertThat(PlatformConstant.AUTH_PATH).isEqualTo("/api/platform/auth");
        assertThat(PlatformConstant.PUBLIC_AUTH_PATH).isEqualTo("/api/platform/public/auth");
        assertThat(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH)
                .isEqualTo("/api/platform/public/auth/password");
        assertThat(PlatformConstant.AUTH_REFRESH_PATH).isEqualTo("/api/platform/auth/refresh");
        assertThat(PlatformConstant.TENANTS_PATH).isEqualTo("/api/platform/tenants");
        assertThat(PlatformConstant.TENANT_ENTERPRISE_PATH)
                .isEqualTo("/api/platform/tenants/{tenantId}/enterprise");
        assertThat(PlatformConstant.USERS_PATH).isEqualTo("/api/platform/users");
        assertThat(PlatformConstant.INVITES_PATH).isEqualTo("/api/platform/invites");
        assertThat(PlatformConstant.PUBLIC_INVITES_PATH).isEqualTo("/api/platform/public/invites");
        assertThat(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH)
                .isEqualTo("/api/platform/public/registration/open");
        assertThat(PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH)
                .isEqualTo("/api/platform/public/registration/access-requests");
        assertThat(PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH)
                .isEqualTo("/api/platform/registration/access-requests");
        assertThat(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH)
                .isEqualTo("/api/platform/settings/registration-mode");
        assertThat(PlatformConstant.apiPath("/auth")).isEqualTo(PlatformConstant.AUTH_PATH);
        assertThat(PlatformConstant.apiPath("tenants")).isEqualTo(PlatformConstant.TENANTS_PATH);
    }
}
