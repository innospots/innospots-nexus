package com.innospots.nexus.base.ui.spec.datasource;

import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link HttpDataSource}.
 */
class HttpDataSourceDeserializationTest {

    @Test
    void deserializesHttpRequestDefinition() {
        PageDsl document = parse("""
                customerDetail:
                  type: http
                  request:
                    method: GET
                    url: /api/customers/${state.selectedId}
                    params:
                      include: profile
                    headers:
                      X-Trace: ${state.traceId}
                    timeout: 5000
                """);

        HttpDataSource dataSource = (HttpDataSource) document.dataSources().get("customerDetail");
        assertThat(dataSource.getType()).isEqualTo("http");
        assertThat(dataSource.getRequest().getMethod()).isEqualTo("GET");
        assertThat(dataSource.getRequest().getUrl()).isEqualTo("/api/customers/${state.selectedId}");
        assertThat(dataSource.getRequest().getParams()).containsEntry("include", "profile");
        assertThat(dataSource.getRequest().getHeaders()).containsEntry("X-Trace", "${state.traceId}");
        assertThat(dataSource.getRequest().getTimeout()).isEqualTo(5000);
    }

    @Test
    void deserializesPostRequestBody() {
        PageDsl document = parse("""
                customerCreate:
                  type: http
                  request:
                    method: POST
                    url: /api/customers
                    body:
                      name: ${state.name}
                """);

        HttpDataSource dataSource = (HttpDataSource) document.dataSources().get("customerCreate");
        assertThat(dataSource.getRequest().getMethod()).isEqualTo("POST");
        assertThat(dataSource.getRequest().getBody()).isInstanceOf(java.util.Map.class);
    }

    private PageDsl parse(String dataSourcesYaml) {
        return PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: http-ds-demo
                dataSources:
                """ + indent(dataSourcesYaml));
    }

    private static String indent(String yaml) {
        return yaml.replaceAll("(?m)^", "  ");
    }
}
