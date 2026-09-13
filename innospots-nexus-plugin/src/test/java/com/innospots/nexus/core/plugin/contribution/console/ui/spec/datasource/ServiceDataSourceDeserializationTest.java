package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PaginationConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ServiceDataSource}.
 */
class ServiceDataSourceDeserializationTest {

    @Test
    void deserializesServiceNameAndParams() {
        PageDsl document = parse("""
                customers:
                  type: service
                  service: customer.list
                  autoLoad: true
                  params:
                    keyword: ${state.keyword}
                    status: active
                """);

        ServiceDataSource dataSource = (ServiceDataSource) document.dataSources().get("customers");
        assertThat(dataSource.getType()).isEqualTo("service");
        assertThat(dataSource.getService()).isEqualTo("customer.list");
        assertThat(dataSource.getAutoLoad()).isTrue();
        assertThat(dataSource.getParams()).containsEntry("keyword", "${state.keyword}");
        assertThat(dataSource.getParams()).containsEntry("status", "active");
    }

    @Test
    void deserializesPaginationBindings() {
        PageDsl document = parse("""
                customers:
                  type: service
                  service: customer.list
                  pagination:
                    page: ${state.page}
                    pageSize: 20
                    totalField: total
                    dataField: records
                """);

        ServiceDataSource dataSource = (ServiceDataSource) document.dataSources().get("customers");
        PaginationConfig pagination = dataSource.getPagination();
        assertThat(pagination.getPage()).isEqualTo("${state.page}");
        assertThat(pagination.getPageSize()).isEqualTo(20);
        assertThat(pagination.getTotalField()).isEqualTo("total");
        assertThat(pagination.getDataField()).isEqualTo("records");
    }

    private PageDsl parse(String dataSourcesYaml) {
        return PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: service-ds-demo
                dataSources:
                """ + indent(dataSourcesYaml));
    }

    private static String indent(String yaml) {
        return yaml.replaceAll("(?m)^", "  ");
    }
}
