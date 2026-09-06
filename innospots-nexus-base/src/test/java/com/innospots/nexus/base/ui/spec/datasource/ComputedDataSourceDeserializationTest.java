package com.innospots.nexus.base.ui.spec.datasource;

import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ComputedDataSource}.
 */
class ComputedDataSourceDeserializationTest {

    @Test
    void deserializesExpressionAndDependsOn() {
        PageDsl document = parse("""
                filteredCustomers:
                  type: computed
                  expression: ${data.customers}
                  dependsOn:
                    - customers
                    - keyword
                """);

        ComputedDataSource dataSource = (ComputedDataSource) document.dataSources().get("filteredCustomers");
        assertThat(dataSource.getType()).isEqualTo("computed");
        assertThat(dataSource.getExpression()).isEqualTo("${data.customers}");
        assertThat(dataSource.getDependsOn()).containsExactly("customers", "keyword");
    }

    @Test
    void deserializesExtensionFieldsThroughJsonAnySetter() {
        PageDsl document = parse("""
                filteredCustomers:
                  type: computed
                  expression: ${data.customers}
                  cacheTtl: 30
                  transform: uppercase
                """);

        ComputedDataSource dataSource = (ComputedDataSource) document.dataSources().get("filteredCustomers");
        assertThat(dataSource.extensions()).containsEntry("cacheTtl", 30);
        assertThat(dataSource.extensions()).containsEntry("transform", "uppercase");
    }

    private PageDsl parse(String dataSourcesYaml) {
        return PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: computed-ds-demo
                dataSources:
                """ + indent(dataSourcesYaml));
    }

    private static String indent(String yaml) {
        return yaml.replaceAll("(?m)^", "  ");
    }
}
