package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link Children} through page fragments and component fields.
 */
class ChildrenDeserializationTest {

    @Test
    void deserializesRootLevelChildrenFragment() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: fragment-demo
                children:
                  - type: Alert
                    props:
                      message: fragment
                """);

        assertThat(document.getChildren().items()).hasSize(1);
        assertThat(((ComponentNode) document.getChildren().items().getFirst()).getType()).isEqualTo("Alert");
    }

    @Test
    void deserializesComponentNodeChildrenField() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Page
                children:
                  - type: Header
                  - component: toolbar
                """, ComponentNode.class);

        assertThat(node.getChildren().isArray()).isTrue();
        assertThat(node.getChildren().items()).hasSize(2);
    }

    @Test
    void deserializesDynamicChildrenOnComponentNode() {
        ComponentNode node = PageDslYamlTestSupport.read("""
                type: Page
                children:
                  source:
                    type: service
                    service: panel.children
                """, ComponentNode.class);

        assertThat(node.getChildren().isArray()).isFalse();
        assertThat(((DslServiceSource) node.getChildren().sourceRef().getSource()).getService())
                .isEqualTo("panel.children");
    }
}
