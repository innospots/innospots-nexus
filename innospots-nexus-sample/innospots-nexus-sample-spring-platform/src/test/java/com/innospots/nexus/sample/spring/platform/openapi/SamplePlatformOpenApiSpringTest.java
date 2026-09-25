package com.innospots.nexus.sample.spring.platform.openapi;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.openapi.domain.vo.OpenApiSpecItemVo;
import com.innospots.nexus.console.openapi.endpoint.OpenApiCatalogEndpoint;
import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
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
        assertThat(yaml).contains("/platform/auth/login");
    }

    @Test
    void consoleBundledYamlContainsCatalogPaths() {
        String yaml = openApiCatalogOperator.readYaml(CONSOLE_SPEC_ID);
        assertThat(yaml).contains("openapi:");
        assertThat(yaml).contains("/openapi/specs");
    }

    @Test
    void endpointGetSpecReturnsYamlResponse() {
        Response response = openApiCatalogEndpoint.getSpec(PLATFORM_SPEC_ID);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).asString().contains("/platform/tenants");
    }

    @Test
    void endpointGetSpecRejectsUnknownId() {
        assertThatThrownBy(() -> openApiCatalogEndpoint.getSpec("unknown-spec-id"))
                .hasMessageContaining("unknown-spec-id");
    }
}
