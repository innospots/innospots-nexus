package com.innospots.nexus.base.ui.spec.node;

import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link ComponentReferenceNode}.
 */
class ComponentReferenceNodeDeserializationTest {

    @Test
    void deserializesComponentReference() {
        ComponentReferenceNode node = PageDslYamlTestSupport.read("""
                component: searchForm
                """, ComponentReferenceNode.class);

        assertThat(node.getComponent()).isEqualTo("searchForm");
    }

    @Test
    void deserializesReferenceOverrides() {
        ComponentReferenceNode node = PageDslYamlTestSupport.read("""
                component: searchForm
                id: compactSearch
                when: ${state.compact}
                props:
                  compact: true
                children:
                  - type: Alert
                events:
                  onSubmit:
                    action: call
                    params:
                      name: search
                """, ComponentReferenceNode.class);

        assertThat(node.getId()).isEqualTo("compactSearch");
        assertThat(node.getWhen()).isEqualTo("${state.compact}");
        assertThat(node.getProps()).containsEntry("compact", true);
        assertThat(node.getChildren().items()).hasSize(1);
        assertThat(node.getEvents()).containsKey("onSubmit");
    }

    @Test
    void bindsReferenceThroughPageBody() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: reference-demo
                components:
                  searchForm:
                    type: Form
                body:
                  component: searchForm
                """);

        ComponentReferenceNode body = (ComponentReferenceNode) document.getBody();
        assertThat(body.getComponent()).isEqualTo("searchForm");
    }

    @Test
    void deserializesReferenceInsideChildrenArray() {
        Children children = PageDslYamlTestSupport.read("""
                - component: toolbar
                - component: searchForm
                  props:
                    compact: true
                """, Children.class);

        assertThat(children.items()).hasSize(2);
        assertThat(((ComponentReferenceNode) children.items().get(1)).getProps())
                .containsEntry("compact", true);
    }
}
