package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.action.ActionOrList;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ActionOrListMapDeserializer} for named page actions.
 */
class ActionOrListMapDeserializerTest {

    @Getter
    @Setter
    static class ActionsHolder {

        @JsonDeserialize(using = ActionOrListMapDeserializer.class)
        private Map<String, ActionOrList> actions = new LinkedHashMap<>();
    }

    @Test
    void deserializesNamedSingleAndMultipleActions() {
        ActionsHolder holder = PageDslYamlTestSupport.read("""
                actions:
                  search:
                    action: reload
                    params:
                      dataSource: customers
                  reset:
                    - action: resetState
                    - action: reload
                      params:
                        dataSource: customers
                """, ActionsHolder.class);

        assertThat(holder.getActions()).containsKeys("search", "reset");
        assertThat(holder.getActions().get("search").actions()).hasSize(1);
        assertThat(holder.getActions().get("reset").actions()).hasSize(2);
    }

    @Test
    void deserializesNullMapAsEmpty() {
        ActionsHolder holder = PageDslYamlTestSupport.read("""
                actions: null
                """, ActionsHolder.class);

        assertThat(holder.getActions()).isNull();
    }

    @Test
    void deserializesThroughPageDslActionsField() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: action-map-demo
                dataSources:
                  customers:
                    type: service
                    service: customer.list
                actions:
                  search:
                    action: reload
                    params:
                      dataSource: customers
                  refresh:
                    - action: message
                      params:
                        text: refreshed
                """);

        assertThat(document.actions()).containsKeys("search", "refresh");
        assertThat(document.actions().get("refresh").actions()).hasSize(1);
    }

    @Test
    void preservesActionEntryOrder() {
        ActionsHolder holder = PageDslYamlTestSupport.read("""
                actions:
                  first:
                    action: setState
                  second:
                    action: reload
                  third:
                    action: message
                """, ActionsHolder.class);

        assertThat(holder.getActions().keySet()).containsExactly("first", "second", "third");
    }
}
