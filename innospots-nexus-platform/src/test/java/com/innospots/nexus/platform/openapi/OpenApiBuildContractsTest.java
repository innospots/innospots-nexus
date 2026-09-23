package com.innospots.nexus.platform.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesPlatformOpenApiSpec() throws Exception {
        Path spec = Path.of("target/generated/openapi/innospots-nexus-platform.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Platform API");
        assertThat(yaml).contains("/platform/auth/login");
        assertThat(yaml).contains("/platform/tenants");
        assertThat(yaml).contains("bearerAuth");
    }
}
