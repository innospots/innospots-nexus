package com.innospots.nexus.core.openapi.scalar;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;
import com.innospots.nexus.core.openapi.catalog.OpenApiCatalogOperator;
import com.scalar.maven.core.ScalarProperties;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiScalarDocumentationTest {

    @Test
    void normalizeDocsPathDefaultsWhenBlank() {
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath(null))
                .isEqualTo(OpenApiCatalogPaths.UI_DEFAULT);
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath(" "))
                .isEqualTo(OpenApiCatalogPaths.UI_DEFAULT);
    }

    @Test
    void normalizeDocsPathEnsuresLeadingSlash() {
        assertThat(OpenApiScalarDocumentation.normalizeDocsPath("docs/api"))
                .isEqualTo("/docs/api");
    }

    @Test
    void scalarJavascriptPathUsesDocumentationPathAndSegment() {
        assertThat(OpenApiCatalogPaths.scalarJavascriptPath("/openapi/ui", "scalar.js"))
                .isEqualTo("/openapi/ui/scalar.js");
        assertThat(OpenApiCatalogPaths.scalarJavascriptPath("docs", null))
                .isEqualTo("/docs/scalar.js");
    }

    @Test
    void specItemUrlPrefixNormalizesSpecsBase() {
        assertThat(OpenApiCatalogPaths.specItemUrlPrefix("openapi/specs/"))
                .isEqualTo("/openapi/specs/");
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

    @Test
    void prepareForServingDoesNotMutateTemplateProperties() {
        ScalarProperties template = OpenApiScalarDocumentation.createDefaultProperties();
        template.setPath("docs/api");
        OpenApiCatalogOperator operator = new OpenApiCatalogOperator();

        ScalarProperties prepared = OpenApiScalarDocumentation.prepareForServing(template, operator);

        assertThat(prepared.getPath()).isEqualTo("/docs/api");
        assertThat(prepared.getSources()).isNotEmpty();
        assertThat(template.getPath()).isEqualTo("docs/api");
        assertThat(template.getSources()).isNull();
    }
}
