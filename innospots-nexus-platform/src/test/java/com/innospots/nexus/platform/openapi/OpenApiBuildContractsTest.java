package com.innospots.nexus.platform.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesPlatformOpenApiSpec() throws Exception {
        Path spec = Path.of("target/classes/META-INF/nexus-openapi/innospots-nexus-platform.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Platform API");
        assertThat(yaml).contains("/api/platform/auth/login");
        assertThat(yaml).contains("/api/platform/tenants");
        assertThat(yaml).contains("/api/platform/users");
        assertThat(yaml).contains("/api/platform/invites");
        assertThat(yaml).contains("/api/platform/registration/access-requests");
        assertThat(yaml).contains("/api/platform/public/invites");
        assertThat(yaml).contains("/api/platform/public/registration/open");
        assertThat(yaml).contains("/api/platform/settings/registration-mode");
        assertThat(yaml).contains("operationId: platformRegistrationSettingsGet");
        assertThat(yaml).doesNotContain("/api/platform/public/settings/registration-mode");
        assertThat(yaml).doesNotContain("operationId: platformPublicRegistrationSettingsGet");
        assertThat(yaml).doesNotContain("/api/platform/auth/onboarding");
        assertThat(yaml).contains("operationId: platformAuthLogin");
        assertThat(yaml).contains("operationId: platformTenantCreate");
        assertThat(yaml).contains("operationId: platformUserCreate");
        assertThat(yaml).contains("operationId: platformUserPage");
        assertThat(yaml).contains("operationId: platformUserUpdate");
        assertThat(yaml).contains("operationId: platformUserUpdateStatus");
        assertThat(yaml).contains("operationId: platformPublicInvitePreview");
        assertThat(yaml).contains("operationId: platformPublicOpenRegistrationSubmit");
        assertThat(yaml).contains("operationId: platformPublicAccessRegistrationSubmit");
        assertThat(yaml).contains("name: PlatformAuth");
        assertThat(yaml).contains("name: PlatformTenant");
        assertThat(yaml).contains("name: PlatformUser");
        assertThat(yaml).contains("bearerAuth");
        assertThat(yaml).contains("TenantCreateRequest:");
        assertThat(yaml).contains("PlatformUserVo:");
    }
}
