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
        assertThat(yaml).contains("/platform/auth/login");
        assertThat(yaml).contains("/platform/tenants");
        assertThat(yaml).contains("/platform/users");
        assertThat(yaml).contains("operationId: platformAuthLogin");
        assertThat(yaml).contains("operationId: platformTenantCreate");
        assertThat(yaml).contains("operationId: platformUserCreate");
        assertThat(yaml).contains("name: PlatformAuth");
        assertThat(yaml).contains("name: PlatformTenant");
        assertThat(yaml).contains("name: PlatformUser");
        assertThat(yaml).contains("bearerAuth");
        assertThat(yaml).contains("TenantCreateRequest:");
        assertThat(yaml).contains("PlatformUserVo:");
    }
}
