package com.innospots.nexus.core.openapi.catalog;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiCatalogOperatorTest {

    @Test
    void listsAndReadsBundledTestSpec() {
        OpenApiCatalogOperator operator = new OpenApiCatalogOperator();
        assertThat(operator.listSpecs())
                .anyMatch(item -> "innospots-nexus-openapi-test".equals(item.specId()));
        assertThat(operator.readYaml("innospots-nexus-openapi-test")).contains("title: Test API");
        assertThat(operator.readOpenApiDocument("innospots-nexus-openapi-test").get("info").get("title").asText())
                .isEqualTo("Test API");
    }
}
