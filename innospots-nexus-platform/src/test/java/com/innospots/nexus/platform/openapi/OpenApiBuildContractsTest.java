package com.innospots.nexus.platform.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.platform.config.PlatformConstant;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesPlatformOpenApiSpec() throws Exception {
        Path spec = Path.of("target/classes/META-INF/nexus-openapi/innospots-nexus-platform.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Platform API");
        assertThat(yaml).contains(PlatformConstant.PUBLIC_AUTH_PATH + "/login");
        assertThat(yaml).contains(PlatformConstant.AUTH_REFRESH_PATH);
        assertThat(yaml).contains(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH + "/reset");
        assertThat(yaml).contains(PlatformConstant.AUTH_PATH + "/logout");
        assertThat(yaml).contains(PlatformConstant.TENANTS_PATH);
        assertThat(yaml).contains(PlatformConstant.TENANT_ENTERPRISE_PATH);
        assertThat(yaml).contains(PlatformConstant.USERS_PATH);
        assertThat(yaml).contains(PlatformConstant.INVITES_PATH);
        assertThat(yaml).contains(PlatformConstant.REGISTRATION_ACCESS_REQUESTS_PATH);
        assertThat(yaml).contains(PlatformConstant.PUBLIC_INVITES_PATH);
        assertThat(yaml).contains(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH);
        assertThat(yaml).contains(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH);
        assertThat(yaml).contains(PlatformConstant.ROLES_PATH);
        assertThat(yaml).contains(PlatformConstant.DICTIONARY_TYPES_PATH);
        assertThat(yaml).contains(PlatformConstant.ROLE_PERMISSIONS_PATH);
        assertThat(yaml).contains(PlatformConstant.AUDIT_LOGS_PATH);
        assertThat(yaml).contains(PlatformConstant.NAVIGATION_MENUS_PATH);
        assertThat(yaml).contains("operationId: platformRegistrationSettingsGet");
        assertThat(yaml).doesNotContain(PlatformConstant.PUBLIC_PREFIX + "/settings/registration-mode");
        assertThat(yaml).doesNotContain("operationId: platformPublicRegistrationSettingsGet");
        assertThat(yaml).doesNotContain(PlatformConstant.AUTH_PATH + "/onboarding");
        assertThat(yaml).contains("operationId: platformAuthLogin");
        assertThat(yaml).contains("operationId: platformTenantPage");
        assertThat(yaml).contains("operationId: platformTenantCreate");
        assertThat(yaml).contains("operationId: platformTenantEnterpriseUpsert");
        assertThat(yaml).contains("name: PlatformEnterpriseProfile");
        assertThat(yaml).contains("operationId: platformUserCreate");
        assertThat(yaml).contains("operationId: platformUserPage");
        assertThat(yaml).contains("operationId: platformUserUpdate");
        assertThat(yaml).contains("operationId: platformUserUpdateStatus");
        assertThat(yaml).contains("operationId: platformPublicInvitePreview");
        assertThat(yaml).contains("operationId: platformPublicOpenRegistrationSubmit");
        assertThat(yaml).contains("operationId: platformPublicAccessRegistrationSubmit");
        assertThat(yaml).contains("name: PlatformPublicAuth");
        assertThat(yaml).contains("name: PlatformAuthSession");
        assertThat(yaml).contains("name: PlatformOrganization");
        assertThat(yaml).contains("name: PlatformUser");
        assertThat(yaml).contains("bearerAuth");
        assertThat(yaml).contains("TenantCreateRequest:");
        assertThat(yaml).contains("PlatformUserVo:");
    }
}
