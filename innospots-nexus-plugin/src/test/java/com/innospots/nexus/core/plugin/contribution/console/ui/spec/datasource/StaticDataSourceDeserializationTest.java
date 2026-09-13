package com.innospots.nexus.core.plugin.contribution.console.ui.spec.datasource;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link StaticDataSource}.
 */
class StaticDataSourceDeserializationTest {

    @Test
    void deserializesStaticValueList() {
        PageDsl document = parse("""
                statusOptions:
                  type: static
                  value:
                    - label: 正常
                      value: active
                    - label: 停用
                      value: inactive
                """);

        StaticDataSource dataSource = (StaticDataSource) document.dataSources().get("statusOptions");
        assertThat(dataSource.getType()).isEqualTo("static");
        assertThat(dataSource.getValue()).isInstanceOf(List.class);
        assertThat(((List<?>) dataSource.getValue())).hasSize(2);
    }

    @Test
    void deserializesStaticScalarValue() {
        PageDsl document = parse("""
                defaultStatus:
                  type: static
                  value: active
                """);

        StaticDataSource dataSource = (StaticDataSource) document.dataSources().get("defaultStatus");
        assertThat(dataSource.getValue()).isEqualTo("active");
    }

    @Test
    void deserializesOptionMappingFields() {
        PageDsl document = parse("""
                statusOptions:
                  type: static
                  autoLoad: true
                  valueField: id
                  labelField: name
                  disabledField: disabled
                  value: []
                """);

        StaticDataSource dataSource = (StaticDataSource) document.dataSources().get("statusOptions");
        assertThat(dataSource.getAutoLoad()).isTrue();
        assertThat(dataSource.getValueField()).isEqualTo("id");
        assertThat(dataSource.getLabelField()).isEqualTo("name");
        assertThat(dataSource.getDisabledField()).isEqualTo("disabled");
    }

    private PageDsl parse(String dataSourcesYaml) {
        return PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: static-ds-demo
                dataSources:
                """ + indent(dataSourcesYaml));
    }

    private static String indent(String yaml) {
        return yaml.replaceAll("(?m)^", "  ");
    }
}
