package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DslNodeDeserializer} for body and placeholder node binding.
 */
class DslNodeDeserializerTest {

    @Getter
    @Setter
    static class BodyHolder {

        @JsonDeserialize(using = DslNodeDeserializer.class)
        private DslNode body;
    }

    @Test
    void deserializesComponentNodeBody() {
        BodyHolder holder = PageDslYamlTestSupport.read("""
                body:
                  type: Page
                  children:
                    - type: Alert
                """, BodyHolder.class);

        assertThat(holder.getBody()).isInstanceOf(ComponentNode.class);
        assertThat(((ComponentNode) holder.getBody()).getType()).isEqualTo("Page");
        assertThat(((ComponentNode) holder.getBody()).getChildren().items()).hasSize(1);
    }

    @Test
    void deserializesComponentReferenceBody() {
        BodyHolder holder = PageDslYamlTestSupport.read("""
                body:
                  component: searchForm
                """, BodyHolder.class);

        assertThat(holder.getBody()).isInstanceOf(ComponentReferenceNode.class);
        assertThat(((ComponentReferenceNode) holder.getBody()).getComponent()).isEqualTo("searchForm");
    }

    @Test
    void deserializesDslSourceRefAsNullBecauseItIsNotDslNode() {
        BodyHolder holder = PageDslYamlTestSupport.read("""
                body:
                  source:
                    type: service
                    service: panel.load
                """, BodyHolder.class);

        assertThat(holder.getBody()).isNull();
    }

    @Test
    void deserializesNullBody() {
        BodyHolder holder = PageDslYamlTestSupport.read("""
                body: null
                """, BodyHolder.class);

        assertThat(holder.getBody()).isNull();
    }

    @Test
    void bindsBodyThroughPageDsl() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: body-demo
                components:
                  searchForm:
                    type: Form
                body:
                  component: searchForm
                """);

        assertThat(document.getBody()).isInstanceOf(ComponentReferenceNode.class);
        assertThat(((ComponentReferenceNode) document.getBody()).getComponent()).isEqualTo("searchForm");
    }

    @Test
    void bindsPlaceholderOnDslSourceRef() {
        DslSourceRef sourceRef = PageDslYamlTestSupport.read("""
                source:
                  type: service
                  service: panel.load
                placeholder:
                  type: Spin
                """, DslSourceRef.class);

        assertThat(sourceRef.getPlaceholder()).isInstanceOf(ComponentNode.class);
        assertThat(((ComponentNode) sourceRef.getPlaceholder()).getType()).isEqualTo("Spin");
    }
}
