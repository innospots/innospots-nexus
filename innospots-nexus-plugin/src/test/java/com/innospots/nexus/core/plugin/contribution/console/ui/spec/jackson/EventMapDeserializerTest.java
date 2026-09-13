package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link EventMapDeserializer} for component event bindings.
 */
class EventMapDeserializerTest {

    @Test
    void deserializesSingleActionEventHandler() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Button
                events:
                  onClick:
                    action: reload
                    params:
                      dataSource: customers
                """, ComponentNode.class);

        ActionOrList onClick = node.getEvents().get("onClick");
        assertThat(onClick.actions()).hasSize(1);
        assertThat(onClick.actions().getFirst().getAction()).isEqualTo("reload");
    }

    @Test
    void deserializesMultipleActionEventHandler() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Button
                events:
                  onClick:
                    - action: setState
                      params:
                        submitting: true
                    - action: call
                      params:
                        name: submit
                """, ComponentNode.class);

        ActionOrList onClick = node.getEvents().get("onClick");
        assertThat(onClick.actions()).hasSize(2);
        assertThat(onClick.actions().get(1).getAction()).isEqualTo("call");
    }

    @Test
    void deserializesMultipleEventNames() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Form
                events:
                  onSubmit:
                    action: call
                    params:
                      name: submit
                  onReset:
                    action: resetState
                """, ComponentNode.class);

        assertThat(node.getEvents()).containsKeys("onSubmit", "onReset");
        assertThat(node.getEvents().get("onReset").actions().getFirst().getAction()).isEqualTo("resetState");
    }

    @Test
    void deserializesNullEventsAsEmptyMap() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Button
                events: null
                """, ComponentNode.class);

        assertThat(node.getEvents()).isNull();
    }

    @Test
    void preservesEventEntryOrder() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Form
                events:
                  onInit:
                    action: setState
                  onSubmit:
                    action: call
                  onReset:
                    action: resetState
                """, ComponentNode.class);

        assertThat(node.getEvents().keySet()).containsExactly("onInit", "onSubmit", "onReset");
    }
}
