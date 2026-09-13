package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ResourceDataSource}.
 */
class ResourceDataSourceDeserializationTest {

    @Test
    void deserializesResourceNameAndParams() {
        PageDsl document = parse("""
                roleOptions:
                  type: resource
                  resource: roles
                  params:
                    tenantId: ${state.tenantId}
                """);

        ResourceDataSource dataSource = (ResourceDataSource) document.dataSources().get("roleOptions");
        assertThat(dataSource.getType()).isEqualTo("resource");
        assertThat(dataSource.getResource()).isEqualTo("roles");
        assertThat(dataSource.getParams()).containsEntry("tenantId", "${state.tenantId}");
    }

    @Test
    void deserializesExtensionFieldsThroughJsonAnySetter() {
        PageDsl document = parse("""
                roleOptions:
                  type: resource
                  resource: roles
                  scope: tenant
                """);

        ResourceDataSource dataSource = (ResourceDataSource) document.dataSources().get("roleOptions");
        assertThat(dataSource.extensions()).containsEntry("scope", "tenant");
    }

    private PageDsl parse(String dataSourcesYaml) {
        return PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: resource-ds-demo
                dataSources:
                """ + indent(dataSourcesYaml));
    }

    private static String indent(String yaml) {
        return yaml.replaceAll("(?m)^", "  ");
    }
}
