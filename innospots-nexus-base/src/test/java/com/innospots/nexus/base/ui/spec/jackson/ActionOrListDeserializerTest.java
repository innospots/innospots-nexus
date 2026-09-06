package com.innospots.nexus.base.ui.spec.jackson;

import com.innospots.nexus.base.ui.spec.action.ActionConfig;
import com.innospots.nexus.base.ui.spec.action.ActionOrList;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ActionOrListDeserializer} for single-object and array union forms.
 */
class ActionOrListDeserializerTest {

    @Test
    void deserializesSingleActionObject() {
        ActionOrList actions = PageDslYamlTestSupport.read("""
                action: reload
                params:
                  dataSource: customers
                """, ActionOrList.class);

        assertThat(actions.actions()).hasSize(1);
        assertThat(actions.actions().getFirst().getAction()).isEqualTo("reload");
        assertThat(actions.actions().getFirst().getParams()).containsEntry("dataSource", "customers");
    }

    @Test
    void deserializesActionArray() {
        ActionOrList actions = PageDslYamlTestSupport.read("""
                - action: resetState
                - action: reload
                  params:
                    dataSource: customers
                """, ActionOrList.class);

        assertThat(actions.actions()).hasSize(2);
        assertThat(actions.actions().get(0).getAction()).isEqualTo("resetState");
        assertThat(actions.actions().get(1).getAction()).isEqualTo("reload");
    }

    @Test
    void deserializesEmptyArray() {
        ActionOrList actions = PageDslYamlTestSupport.read("[]", ActionOrList.class);

        assertThat(actions.actions()).isEmpty();
        assertThat(actions.isEmpty()).isTrue();
    }

    @Test
    void deserializesNullAsEmptyList() {
        ActionOrList actions = PageDslYamlTestSupport.read("null", ActionOrList.class);

        assertThat(actions).isNull();
    }

    @Test
    void deserializesActionWithIdAndCondition() {
        ActionOrList actions = PageDslYamlTestSupport.read("""
                id: searchResult
                action: reload
                condition: ${state.keyword}
                params:
                  dataSource: customers
                """, ActionOrList.class);

        ActionConfig action = actions.actions().getFirst();
        assertThat(action.getId()).isEqualTo("searchResult");
        assertThat(action.getCondition()).isEqualTo("${state.keyword}");
    }

    @Test
    void roundTripsSingleActionAsObject() {
        ActionConfig step = new ActionConfig();
        step.setAction("setState");
        step.setParams(java.util.Map.of("ready", true));
        ActionOrList original = new ActionOrList(java.util.List.of(step));

        ActionOrList restored = PageDslYamlTestSupport.read(
                PageDslYamlTestSupport.write(original),
                ActionOrList.class);

        assertThat(restored.actions()).hasSize(1);
        assertThat(restored.actions().getFirst().getAction()).isEqualTo("setState");
    }

    @Test
    void roundTripsMultipleActionsAsArray() {
        ActionConfig first = new ActionConfig();
        first.setAction("resetState");
        ActionConfig second = new ActionConfig();
        second.setAction("reload");
        ActionOrList original = new ActionOrList(java.util.List.of(first, second));

        ActionOrList restored = PageDslYamlTestSupport.read(
                PageDslYamlTestSupport.write(original),
                ActionOrList.class);

        assertThat(restored.actions()).hasSize(2);
        assertThat(restored.actions().get(1).getAction()).isEqualTo("reload");
    }
}
