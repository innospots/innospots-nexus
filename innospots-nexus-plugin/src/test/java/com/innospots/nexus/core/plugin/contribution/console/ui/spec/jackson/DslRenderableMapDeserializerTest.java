package com.innospots.nexus.core.plugin.contribution.console.ui.spec.jackson;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.ComponentReferenceNode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslRenderable;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.node.DslSourceRef;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link DslRenderableMapDeserializer} for named reusable components.
 */
class DslRenderableMapDeserializerTest {

    @Getter
    @Setter
    static class ComponentsHolder {

        @JsonDeserialize(using = DslRenderableMapDeserializer.class)
        private Map<String, DslRenderable> components = new LinkedHashMap<>();
    }

    @Test
    void deserializesNamedComponentNodeReferenceAndSourceRef() {
        ComponentsHolder holder = PageDslYamlTestSupport.read("""
                components:
                  searchForm:
                    type: Form
                    props:
                      name: search
                  toolbar:
                    component: searchForm
                  extensionPanel:
                    source:
                      type: service
                      service: customer.extensionPanel
                    placeholder:
                      type: Alert
                """, ComponentsHolder.class);

        assertThat(holder.getComponents()).containsKeys("searchForm", "toolbar", "extensionPanel");
        assertThat(holder.getComponents().get("searchForm")).isInstanceOf(ComponentNode.class);
        assertThat(holder.getComponents().get("toolbar")).isInstanceOf(ComponentReferenceNode.class);
        assertThat(holder.getComponents().get("extensionPanel")).isInstanceOf(DslSourceRef.class);
    }

    @Test
    void deserializesNullMapAsEmpty() {
        ComponentsHolder holder = PageDslYamlTestSupport.read("""
                components: null
                """, ComponentsHolder.class);

        assertThat(holder.getComponents()).isNull();
    }

    @Test
    void deserializesThroughPageDslComponentsField() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: components-demo
                components:
                  searchForm:
                    type: Form
                  toolbar:
                    type: Space
                """);

        assertThat(document.components()).containsKeys("searchForm", "toolbar");
        assertThat(document.components().get("searchForm")).isInstanceOf(ComponentNode.class);
    }

    @Test
    void preservesComponentEntryOrder() {
        ComponentsHolder holder = PageDslYamlTestSupport.read("""
                components:
                  header:
                    type: Header
                  body:
                    type: Body
                  footer:
                    type: Footer
                """, ComponentsHolder.class);

        assertThat(holder.getComponents().keySet()).containsExactly("header", "body", "footer");
    }
}
