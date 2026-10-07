package com.innospots.nexus.core.openapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NexusOpenApiDefinitionContractsTest {

    @Test
    void exposesSharedSecuritySchemeName() {
        assertThat(NexusOpenApiSecurityNames.BEARER_AUTH).isEqualTo("bearerAuth");
        assertThat(NexusAuthenticatedApi.class.isAnnotation()).isTrue();
    }
}
