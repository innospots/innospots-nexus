package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.permission.PermissionDenied;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ComponentNode}.
 */
class ComponentNodeDeserializationTest {

    @Test
    void deserializesTypePropsIdAndWhen() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Form
                id: searchForm
                when: ${state.ready}
                props:
                  name: search
                  compact: true
                """, ComponentNode.class);

        assertThat(node.getType()).isEqualTo("Form");
        assertThat(node.getId()).isEqualTo("searchForm");
        assertThat(node.getWhen()).isEqualTo("${state.ready}");
        assertThat(node.getProps()).containsEntry("name", "search");
        assertThat(node.getProps()).containsEntry("compact", true);
    }

    @Test
    void deserializesChildrenEventsAndPermission() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Page
                permission:
                  - customer:edit
                  - customer:admin
                children:
                  - type: Alert
                    props:
                      message: ready
                events:
                  onInit:
                    action: setState
                """, ComponentNode.class);

        assertThat(node.getPermission().getKind()).isEqualTo(PermissionConfig.PermissionKind.ANY_OF);
        assertThat(node.getChildren().items()).hasSize(1);
        assertThat(node.getEvents()).containsKey("onInit");
    }

    @Test
    void deserializesNestedComponentTree() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Page
                children:
                  - type: Card
                    children:
                      - type: Button
                        props:
                          text: 保存
                """, ComponentNode.class);

        ComponentNode card = (ComponentNode) node.getChildren().items().getFirst();
        ComponentNode button = (ComponentNode) card.getChildren().items().getFirst();
        assertThat(card.getType()).isEqualTo("Card");
        assertThat(button.getType()).isEqualTo("Button");
    }

    @Test
    void bindsComponentNodeThroughPageBody() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: component-node-demo
                body:
                  type: Page
                  permission:
                    code: customer:view
                    denied: disabled
                """);

        ComponentNode body = (ComponentNode) document.getBody();
        assertThat(body.getType()).isEqualTo("Page");
        assertThat(body.getPermission().getDenied()).isEqualTo(PermissionDenied.DISABLED);
    }
}
