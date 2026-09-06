package com.innospots.nexus.base.ui.spec.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.base.ui.spec.node.Children;
import com.innospots.nexus.base.ui.spec.node.ComponentNode;
import com.innospots.nexus.base.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.base.ui.spec.node.DslRenderable;
import com.innospots.nexus.base.ui.spec.node.DslServiceSource;
import com.innospots.nexus.base.ui.spec.node.DslSourceRef;
import com.innospots.nexus.base.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DslRenderableDeserializer} for component, reference, and source-ref shapes.
 */
class DslRenderableDeserializerTest {

    @Getter
    @Setter
    static class RenderableHolder {

        @JsonDeserialize(using = DslRenderableDeserializer.class)
        private DslRenderable node;
    }

    @Test
    void deserializesComponentNodeByTypeField() {
        RenderableHolder holder = PageDslYamlTestSupport.read("""
                node:
                  type: Form
                  props:
                    name: search
                """, RenderableHolder.class);

        assertThat(holder.getNode()).isInstanceOf(ComponentNode.class);
        assertThat(((ComponentNode) holder.getNode()).getType()).isEqualTo("Form");
    }

    @Test
    void deserializesComponentReferenceByComponentField() {
        RenderableHolder holder = PageDslYamlTestSupport.read("""
                node:
                  component: searchForm
                  props:
                    compact: true
                """, RenderableHolder.class);

        assertThat(holder.getNode()).isInstanceOf(ComponentReferenceNode.class);
        assertThat(((ComponentReferenceNode) holder.getNode()).getComponent()).isEqualTo("searchForm");
    }

    @Test
    void deserializesDslSourceRefBySourceField() {
        RenderableHolder holder = PageDslYamlTestSupport.read("""
                node:
                  source:
                    type: service
                    service: customer.extensionPanel
                  placeholder:
                    type: Alert
                """, RenderableHolder.class);

        assertThat(holder.getNode()).isInstanceOf(DslSourceRef.class);
        DslSourceRef sourceRef = (DslSourceRef) holder.getNode();
        assertThat(((DslServiceSource) sourceRef.getSource()).getService())
                .isEqualTo("customer.extensionPanel");
    }

    @Test
    void deserializesNullRenderable() {
        RenderableHolder holder = PageDslYamlTestSupport.read("""
                node: null
                """, RenderableHolder.class);

        assertThat(holder.getNode()).isNull();
    }

    @Test
    void resolvesSourceRefBeforeComponentReferenceInChildrenArray() {
        Children children = PageDslYamlTestSupport.read("""
                - source:
                    type: service
                    service: panel.load
                - component: toolbar
                """, Children.class);

        assertThat(children.items().getFirst()).isInstanceOf(DslSourceRef.class);
        assertThat(children.items().get(1)).isInstanceOf(ComponentReferenceNode.class);
    }

    @Test
    void prefersComponentFieldOverInlineTypeShape() {
        RenderableHolder holder = PageDslYamlTestSupport.read("""
                node:
                  component: searchForm
                  props:
                    compact: true
                """, RenderableHolder.class);

        assertThat(holder.getNode()).isInstanceOf(ComponentReferenceNode.class);
        assertThat(holder.getNode()).isNotInstanceOf(ComponentNode.class);
    }
}
