package com.innospots.nexus.portal.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesPortalOpenApiSpec() throws Exception {
        Path spec = Path.of("target/generated/openapi/innospots-nexus-portal.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Portal API");
        assertThat(yaml).contains("/tenant/auth/login");
        assertThat(yaml).contains("/tenant/scope/select-workspace");
        assertThat(yaml).contains("/tenant/scope/select-project");
        assertThat(yaml).contains("operationId: tenantAuthRegister");
        assertThat(yaml).contains("operationId: tenantAuthCaptchaIssue");
        assertThat(yaml).contains("operationId: tenantAuthLogin");
        assertThat(yaml).contains("operationId: tenantAuthSelectTenant");
        assertThat(yaml).contains("operationId: tenantAuthRefresh");
        assertThat(yaml).contains("operationId: tenantAuthLogout");
        assertThat(yaml).contains("operationId: tenantAuthPasswordChange");
        assertThat(yaml).contains("operationId: tenantAuthPasswordReset");
        assertThat(yaml).contains("operationId: tenantScopeSelectWorkspace");
        assertThat(yaml).contains("operationId: tenantScopeSelectProject");
        assertThat(yaml).contains("$ref: \"#/components/schemas/AuthLoginRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/AuthCaptchaIssueRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/TokenRefreshRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/PasswordChangeRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/PasswordResetRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/TenantRegisterRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/SelectTenantRequest\"");
        assertThat(yaml).contains("$ref: \"#/components/schemas/SelectProjectRequest\"");
        assertThat(yaml).contains("AuthLoginRequest:");
        assertThat(yaml).contains("SelectProjectRequest:");
        assertThat(yaml).contains("bearerAuth");
    }
}
