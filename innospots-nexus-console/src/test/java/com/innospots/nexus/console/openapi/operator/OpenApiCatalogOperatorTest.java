package com.innospots.nexus.console.openapi.operator;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiCatalogOperatorTest {

    @Test
    void listsAndReadsBundledTestSpec() {
        OpenApiCatalogOperator operator = new OpenApiCatalogOperator();
        assertThat(operator.listSpecs())
                .anyMatch(item -> "innospots-nexus-openapi-test".equals(item.specId()));
        assertThat(operator.readYaml("innospots-nexus-openapi-test")).contains("title: Test API");
    }
}
