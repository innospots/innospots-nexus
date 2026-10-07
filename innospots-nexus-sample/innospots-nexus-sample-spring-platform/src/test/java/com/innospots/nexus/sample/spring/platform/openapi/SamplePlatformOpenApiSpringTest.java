package com.innospots.nexus.sample.spring.platform.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogEndpoint;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.innospots.nexus.core.openapi.catalog.OpenApiSpecItemVo;
import com.innospots.nexus.sample.spring.platform.openapi.support.OpenApiJerseyTestApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 在 sample 依赖的 classpath 上验证 OpenAPI 目录 Spring 装配与端点行为。
 */
@SpringBootTest(classes = OpenApiJerseyTestApplication.class)
class SamplePlatformOpenApiSpringTest {

    private static final String PLATFORM_SPEC_ID = "innospots-nexus-platform";

    private static final String CONSOLE_SPEC_ID = "innospots-nexus-console";

    @Autowired
    private OpenApiCatalogEndpoint openApiCatalogEndpoint;

    @Autowired
    private OpenApiCatalogOperator openApiCatalogOperator;

    @Test
    void openApiCatalogBeansAreWired() {
        assertThat(openApiCatalogEndpoint).isNotNull();
        assertThat(openApiCatalogOperator).isNotNull();
    }

    @Test
    void listsPlatformAndConsoleSpecsFromClasspath() {
        assertThat(openApiCatalogOperator.listSpecs().stream().map(OpenApiSpecItemVo::specId))
                .contains(PLATFORM_SPEC_ID, CONSOLE_SPEC_ID);
    }

    @Test
    void endpointListSpecsReturnsWrappedCatalog() {
        R<java.util.List<OpenApiSpecItemVo>> response = openApiCatalogEndpoint.listSpecs();
        assertThat(response.success()).isTrue();
        assertThat(response.data().stream().map(OpenApiSpecItemVo::specId))
                .contains(PLATFORM_SPEC_ID, CONSOLE_SPEC_ID);
    }

    @Test
    void platformBundledYamlContainsDocumentedPaths() {
        String yaml = openApiCatalogOperator.readYaml(PLATFORM_SPEC_ID);
        assertThat(yaml).contains("openapi:");
        assertThat(yaml).contains("/platform/tenants");
        assertThat(yaml).contains("/platform/public/auth/login");
        assertThat(yaml).contains("/platform/auth/refresh");
    }

    @Test
    void consoleBundledYamlContainsCatalogPaths() {
        String yaml = openApiCatalogOperator.readYaml(CONSOLE_SPEC_ID);
        assertThat(yaml).contains("openapi:");
        assertThat(yaml).contains("/api/d/nexus/status");
    }

    @Test
    void endpointGetSpecReturnsOpenApiJsonDocument() {
        JsonNode document = openApiCatalogEndpoint.getSpec(PLATFORM_SPEC_ID);
        assertThat(document.get("openapi").asText()).startsWith("3.");
        assertThat(document.get("paths").toString()).contains("/platform/tenants");
    }

    @Test
    void endpointGetSpecRejectsUnknownId() {
        assertThatThrownBy(() -> openApiCatalogEndpoint.getSpec("unknown-spec-id"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("unknown-spec-id");
    }
}
