package com.innospots.nexus.console.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesConsoleOpenApiSpec() throws Exception {
        Path spec = Path.of("target/generated/openapi/innospots-nexus-console.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Console API");
        assertThat(yaml).contains("/console/roles");
        assertThat(yaml).contains("operationId: rolePage");
        assertThat(yaml).contains("bearerAuth");
    }
}
