package com.innospots.nexus.platform.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlatformConstantTest {

    @Test
    void platformPathsMatchOperationalEndpoints() {
        assertThat(PlatformConstant.API_PREFIX).isEqualTo("/api/d/platform");
        assertThat(PlatformConstant.AUTH_PATH).isEqualTo("/api/d/platform/auth");
        assertThat(PlatformConstant.PUBLIC_AUTH_PATH).isEqualTo("/api/d/platform/public/auth");
        assertThat(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH)
                .isEqualTo("/api/d/platform/public/auth/password");
        assertThat(PlatformConstant.AUTH_REFRESH_PATH).isEqualTo("/api/d/platform/auth/refresh");
        assertThat(PlatformConstant.TENANTS_PATH).isEqualTo("/api/d/platform/tenants");
        assertThat(PlatformConstant.TENANT_ENTERPRISE_PATH)
                .isEqualTo("/api/d/platform/tenants/{tenantId}/enterprise");
        assertThat(PlatformConstant.USERS_PATH).isEqualTo("/api/d/platform/users");
        assertThat(PlatformConstant.ROLES_PATH).isEqualTo("/api/d/platform/roles");
        assertThat(PlatformConstant.ROLE_BINDINGS_PATH)
                .isEqualTo("/api/d/platform/roles/{roleId}/bindings");
        assertThat(PlatformConstant.DICTIONARY_TYPES_PATH)
                .isEqualTo("/api/d/platform/dictionary-types");
        assertThat(PlatformConstant.DICTIONARY_ITEMS_PATH)
                .isEqualTo("/api/d/platform/dictionary-types/{typeCode}/items");
        assertThat(PlatformConstant.ROLE_PERMISSIONS_PATH)
                .isEqualTo("/api/d/platform/roles/{roleId}/permissions");
        assertThat(PlatformConstant.ME_PERMISSIONS_PATH).isEqualTo("/api/d/platform/me/permissions");
        assertThat(PlatformConstant.AUDIT_LOGS_PATH).isEqualTo("/api/d/platform/audit-logs");
        assertThat(PlatformConstant.NAVIGATION_MENUS_PATH)
                .isEqualTo("/api/d/platform/navigation/menus");
        assertThat(PlatformConstant.INVITES_PATH).isEqualTo("/api/d/platform/invites");
        assertThat(PlatformConstant.PUBLIC_INVITES_PATH).isEqualTo("/api/d/platform/public/invites");
        assertThat(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH)
                .isEqualTo("/api/d/platform/public/registration/open");
        assertThat(PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH)
                .isEqualTo("/api/d/platform/public/registration/access-requests");
        assertThat(PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH)
                .isEqualTo("/api/d/platform/registration/access-requests");
        assertThat(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH)
                .isEqualTo("/api/d/platform/settings/registration-mode");
        assertThat(PlatformConstant.apiPath("/auth")).isEqualTo(PlatformConstant.AUTH_PATH);
        assertThat(PlatformConstant.apiPath("tenants")).isEqualTo(PlatformConstant.TENANTS_PATH);
        assertThat(PlatformConstant.apiPath("/roles")).isEqualTo(PlatformConstant.ROLES_PATH);
    }
}
