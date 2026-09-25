package com.innospots.nexus.console.openapi.scalar;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.openapi.operator.OpenApiCatalogOperator;
import com.scalar.maven.core.ScalarProperties;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiScalarDocumentationTest {

    @Test
    void normalizeDocsPathDefaultsWhenBlank() {
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath(null))
                .isEqualTo(OpenApiScalarDocumentation.DEFAULT_DOCS_PATH);
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath(" "))
                .isEqualTo(OpenApiScalarDocumentation.DEFAULT_DOCS_PATH);
    }

    @Test
    void normalizeDocsPathEnsuresLeadingSlash() {
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath("docs/api"))
                .isEqualTo("/docs/api");
    }

    @Test
    void renderedHtmlIncludesCatalogSpecIds() throws Exception {
        ScalarProperties properties = OpenApiScalarDocumentation.createDefaultProperties();
        OpenApiCatalogOperator operator = new OpenApiCatalogOperator();
        String html = OpenApiScalarDocumentation.renderDocumentationHtml(properties, operator);
        assertThat(html).containsIgnoringCase("scalar");
        assertThat(operator.listSpecs()).isNotEmpty();
        assertThat(html).contains(operator.listSpecs().getFirst().specId());
    }
}
