package com.innospots.nexus.base.ui.spec.action;

import com.innospots.nexus.base.ui.spec.permission.PermissionConfig;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ActionConfig}.
 */
class ActionConfigDeserializationTest {

    @Test
    void deserializesRequiredActionField() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                """, ActionConfig.class);

        assertThat(action.getAction()).isEqualTo("reload");
    }

    @Test
    void deserializesIdParamsConditionAndPermission() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                id: searchResult
                action: reload
                condition: ${state.keyword}
                permission: customer:view
                params:
                  dataSource: customers
                  page: ${state.page}
                """, ActionConfig.class);

        assertThat(action.getId()).isEqualTo("searchResult");
        assertThat(action.getCondition()).isEqualTo("${state.keyword}");
        assertThat(action.getPermission().getCode()).isEqualTo("customer:view");
        assertThat(action.getParams()).containsEntry("dataSource", "customers");
        assertThat(action.getParams()).containsEntry("page", "${state.page}");
    }

    @Test
    void deserializesBooleanCondition() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: message
                condition: true
                """, ActionConfig.class);

        assertThat(action.getCondition()).isEqualTo(true);
    }

    @Test
    void deserializesNestedParamsMap() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: setState
                params:
                  filters:
                    keyword: ${state.keyword}
                    status: active
                """, ActionConfig.class);

        assertThat(action.getParams().get("filters")).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) action.getParams().get("filters");
        assertThat(filters).containsEntry("keyword", "${state.keyword}");
        assertThat(filters).containsEntry("status", "active");
    }

    @Test
    void roundTripsActionConfig() {
        ActionConfig original = new ActionConfig();
        original.setId("result");
        original.setAction("call");
        original.setCondition(false);
        original.setPermission(PermissionConfig.code("customer:edit"));
        original.setParams(Map.of("name", "search"));

        ActionConfig restored = PageDslYamlTestSupport.read(
                PageDslYamlTestSupport.write(original),
                ActionConfig.class);

        assertThat(restored.getId()).isEqualTo("result");
        assertThat(restored.getAction()).isEqualTo("call");
        assertThat(restored.getCondition()).isEqualTo(false);
        assertThat(restored.getPermission().getCode()).isEqualTo("customer:edit");
        assertThat(restored.getParams()).containsEntry("name", "search");
    }
}
