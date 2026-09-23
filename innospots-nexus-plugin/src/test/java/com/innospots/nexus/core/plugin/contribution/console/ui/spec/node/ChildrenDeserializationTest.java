package com.innospots.nexus.core.plugin.contribution.console.ui.spec.node;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试通过页面片段与组件字段对 {@link Children} 的 YAML 反序列化。
 * @author Smars
 * @date 2026/09/13
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
