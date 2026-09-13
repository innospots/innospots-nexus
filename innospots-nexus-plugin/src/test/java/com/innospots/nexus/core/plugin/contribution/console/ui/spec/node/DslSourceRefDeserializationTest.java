package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests YAML deserialization for {@link DslSourceRef} dynamic fragments.
 */
class DslSourceRefDeserializationTest {

    @Test
    void deserializesServiceSourceWithPlaceholder() {
        DslSourceRef sourceRef = PageDslYamlTestSupport.read("""
                source:
                  type: service
                  service: customer.extensionPanel
                  params:
                    customerId: ${state.selectedId}
                placeholder:
                  type: Alert
                  props:
                    message: 加载中
                """, DslSourceRef.class);

        DslServiceSource source = (DslServiceSource) sourceRef.getSource();
        assertThat(source.getService()).isEqualTo("customer.extensionPanel");
        assertThat(source.getParams()).containsEntry("customerId", "${state.selectedId}");
        assertThat(((ComponentNode) sourceRef.getPlaceholder()).getType()).isEqualTo("Alert");
    }

    @Test
    void deserializesHttpSource() {
        DslSourceRef sourceRef = PageDslYamlTestSupport.read("""
                source:
                  type: http
                  request:
                    method: GET
                    url: /api/panels/${state.panelId}
                """, DslSourceRef.class);

        DslHttpSource source = (DslHttpSource) sourceRef.getSource();
        assertThat(source.getRequest().getMethod()).isEqualTo("GET");
        assertThat(source.getRequest().getUrl()).isEqualTo("/api/panels/${state.panelId}");
    }

    @Test
    void bindsDslSourceRefThroughPageComponents() {
        PageDsl document = PageDslYamlTestSupport.parsePage("""
                dsl: '1.0'
                page:
                  id: source-ref-demo
                components:
                  extensionPanel:
                    source:
                      type: service
                      service: customer.extensionPanel
                    placeholder:
                      type: Spin
                """);

        DslSourceRef extensionPanel = (DslSourceRef) document.components().get("extensionPanel");
        assertThat(((DslServiceSource) extensionPanel.getSource()).getService())
                .isEqualTo("customer.extensionPanel");
        assertThat(((ComponentNode) extensionPanel.getPlaceholder()).getType()).isEqualTo("Spin");
    }

    @Test
    void deserializesSourceRefAsChildrenDynamicForm() {
        Children children = PageDslYamlTestSupport.read("""
                source:
                  type: service
                  service: panel.load
                """, Children.class);

        assertThat(children.isArray()).isFalse();
        assertThat(((DslServiceSource) children.sourceRef().getSource()).getService()).isEqualTo("panel.load");
    }
}
