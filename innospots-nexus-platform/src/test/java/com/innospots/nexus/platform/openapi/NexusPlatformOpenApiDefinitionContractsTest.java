package com.innospots.nexus.platform.openapi;

import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;

import static org.assertj.core.api.Assertions.assertThat;

class NexusPlatformOpenApiDefinitionContractsTest {

    @Test
    void definitionCarriesPathForJaxRsScanner() {
        assertThat(NexusPlatformOpenApiDefinition.class.getAnnotation(Path.class).value()).isEqualTo("/platform");
        assertThat(NexusPlatformOpenApiDefinition.class.getAnnotation(OpenAPIDefinition.class)).isNotNull();
        assertThat(NexusPlatformOpenApiDefinition.class.getAnnotation(SecurityScheme.class).securitySchemeName())
                .isEqualTo(NexusOpenApiSecurityNames.BEARER_AUTH);
    }

    @Test
    void definitionDeclaresPlatformTags() {
        Tag[] tags = NexusPlatformOpenApiDefinition.class.getAnnotation(OpenAPIDefinition.class).tags();
        assertThat(tags).extracting(Tag::name)
                .containsExactly("PlatformAuth", "PlatformTenant", "PlatformUser");
    }
}
