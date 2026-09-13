package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.Children;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslServiceSource;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link ChildrenDeserializer} for array, single-node, and dynamic source forms.
 */
class ChildrenDeserializerTest {

    @Test
    void deserializesInlineArrayChildren() {
        Children children = PageDslYamlTestSupport.read("""
                - type: Button
                  props:
                    text: 保存
                - component: searchForm
                """, Children.class);

        assertThat(children.isArray()).isTrue();
        assertThat(children.items()).hasSize(2);
        assertThat(children.items().getFirst()).isInstanceOf(ComponentNode.class);
        assertThat(children.items().get(1)).isInstanceOf(ComponentReferenceNode.class);
    }

    @Test
    void deserializesDynamicSourceRefChildren() {
        Children children = PageDslYamlTestSupport.read("""
                source:
                  type: service
                  service: customer.extensionPanel
                  params:
                    customerId: ${state.selectedId}
                """, Children.class);

        assertThat(children.isArray()).isFalse();
        assertThat(children.sourceRef()).isNotNull();
        DslServiceSource source = (DslServiceSource) children.sourceRef().getSource();
        assertThat(source.getService()).isEqualTo("customer.extensionPanel");
        assertThat(source.getParams()).containsEntry("customerId", "${state.selectedId}");
    }

    @Test
    void deserializesSingleInlineNodeAsOneItemList() {
        Children children = PageDslYamlTestSupport.read("""
                type: Alert
                props:
                  message: ready
                """, Children.class);

        assertThat(children.isArray()).isTrue();
        assertThat(children.items()).hasSize(1);
        assertThat(((ComponentNode) children.items().getFirst()).getType()).isEqualTo("Alert");
    }

    @Test
    void deserializesNullAsEmptyChildren() {
        Children children = PageDslYamlTestSupport.read("null", Children.class);

        assertThat(children).isNull();
    }

    @Test
    void deserializesEmptyArray() {
        Children children = PageDslYamlTestSupport.read("[]", Children.class);

        assertThat(children.items()).isEmpty();
    }

    @Test
    void resolvesComponentReferenceByComponentField() {
        Children children = PageDslYamlTestSupport.read("""
                - component: toolbar
                """, Children.class);

        ComponentReferenceNode reference = (ComponentReferenceNode) children.items().getFirst();
        assertThat(reference.getComponent()).isEqualTo("toolbar");
    }

    @Test
    void resolvesDslSourceRefBySourceField() {
        Children children = PageDslYamlTestSupport.read("""
                - source:
                    type: service
                    service: panel.load
                  placeholder:
                    type: Spin
                """, Children.class);

        DslSourceRef sourceRef = (DslSourceRef) children.items().getFirst();
        assertThat(((DslServiceSource) sourceRef.getSource()).getService()).isEqualTo("panel.load");
        assertThat(((ComponentNode) sourceRef.getPlaceholder()).getType()).isEqualTo("Spin");
    }
}
