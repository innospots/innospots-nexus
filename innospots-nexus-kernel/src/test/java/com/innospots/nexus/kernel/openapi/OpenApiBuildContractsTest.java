package com.innospots.nexus.kernel.openapi;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiBuildContractsTest {

    @Test
    void buildGeneratesKernelOpenApiSpec() throws Exception {
        Path spec = Path.of("target/generated/openapi/innospots-nexus-kernel.yaml");
        assertThat(spec).exists();
        String yaml = Files.readString(spec);
        assertThat(yaml).contains("Innospots Nexus Kernel API");
        assertThat(yaml).contains("/tenant/auth/login");
        assertThat(yaml).contains("/tenant/scope/select-workspace");
        assertThat(yaml).contains("bearerAuth");
    }
}
