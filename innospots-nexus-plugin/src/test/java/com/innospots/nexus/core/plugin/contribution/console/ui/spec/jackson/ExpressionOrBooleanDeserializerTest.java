package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ExpressionOrBooleanDeserializer} for expression strings and boolean literals.
 */
class ExpressionOrBooleanDeserializerTest {

    @Test
    void deserializesBooleanLiteral() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                condition: true
                """, ActionConfig.class);

        assertThat(action.getCondition()).isEqualTo(true);
    }

    @Test
    void deserializesFalseBooleanLiteral() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                condition: false
                """, ActionConfig.class);

        assertThat(action.getCondition()).isEqualTo(false);
    }

    @Test
    void deserializesExpressionString() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                condition: ${state.ready}
                """, ActionConfig.class);

        assertThat(action.getCondition()).isEqualTo("${state.ready}");
    }

    @Test
    void deserializesNullCondition() {
        ActionConfig action = PageDslYamlTestSupport.read("""
                action: reload
                condition: null
                """, ActionConfig.class);

        assertThat(action.getCondition()).isNull();
    }

    @Test
    void deserializesWhenFieldOnComponentNode() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Alert
                when: ${data.customers.length > 0}
                """, ComponentNode.class);

        assertThat(node.getWhen()).isEqualTo("${data.customers.length > 0}");
    }

    @Test
    void deserializesBooleanWhenFieldOnComponentNode() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Alert
                when: false
                """, ComponentNode.class);

        assertThat(node.getWhen()).isEqualTo(false);
    }

    @Test
    void roundTripsExpressionCondition() {
        ActionConfig original = new ActionConfig();
        original.setAction("reload");
        original.setCondition("${state.keyword}");

        ActionConfig restored = PageDslYamlTestSupport.read(
                PageDslYamlTestSupport.write(original),
                ActionConfig.class);

        assertThat(restored.getCondition()).isEqualTo("${state.keyword}");
    }

    @Getter
    @Setter
    static class ConditionHolder {

        @JsonDeserialize(using = ExpressionOrBooleanDeserializer.class)
        private Object condition;
    }

    @Test
    void deserializesThroughDirectDeserializerBinding() {
        ConditionHolder holder = PageDslYamlTestSupport.read("""
                condition: ${actions.search}
                """, ConditionHolder.class);

        assertThat(holder.getCondition()).isEqualTo("${actions.search}");
    }
}
