package com.innospots.nexus.core.plugin.contribution.console.ui.spec.action;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.LifecycleConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.support.PageDslYamlTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 测试生命周期与页面上下文中 {@link ActionOrList} 的 YAML 反序列化。
 * @author Smars
 * @date 2026/09/13
 */
class ActionOrListDeserializationTest {

    @Test
    void deserializesLifecycleSingleAndArrayHooks() {
        LifecycleConfig lifecycle = PageDslYamlTestSupport.read("""
                onInit:
                  action: setState
                  params:
                    ready: true
                onLoad: []
                onReady:
                  - action: message
                    params:
                      text: ready
                  - action: call
                    params:
                      name: refresh
                """, LifecycleConfig.class);

        assertThat(lifecycle.getOnInit().actions()).hasSize(1);
        assertThat(lifecycle.getOnLoad().actions()).isEmpty();
        assertThat(lifecycle.getOnReady().actions()).hasSize(2);
    }

    @Test
    void deserializesAllLifecycleHookNames() {
        LifecycleConfig lifecycle = PageDslYamlTestSupport.read("""
                onInit:
                  action: setState
                onLoad:
                  action: reload
                onReady:
                  action: message
                onShow:
                  action: message
                onHide:
                  action: message
                onDestroy:
                  action: resetState
                """, LifecycleConfig.class);

        assertThat(lifecycle.getOnInit().actions().getFirst().getAction()).isEqualTo("setState");
        assertThat(lifecycle.getOnLoad().actions().getFirst().getAction()).isEqualTo("reload");
        assertThat(lifecycle.getOnReady().actions().getFirst().getAction()).isEqualTo("message");
        assertThat(lifecycle.getOnShow().actions().getFirst().getAction()).isEqualTo("message");
        assertThat(lifecycle.getOnHide().actions().getFirst().getAction()).isEqualTo("message");
        assertThat(lifecycle.getOnDestroy().actions().getFirst().getAction()).isEqualTo("resetState");
    }

    @Test
    void roundTripsActionOrListUnionSerialization() {
        ActionConfig step = new ActionConfig();
        step.setAction("reload");
        ActionOrList original = new ActionOrList(java.util.List.of(step));

        ActionOrList restored = PageDslYamlTestSupport.read(
                PageDslYamlTestSupport.write(original),
                ActionOrList.class);

        assertThat(restored.actions()).hasSize(1);
        assertThat(restored.actions().getFirst().getAction()).isEqualTo("reload");
    }
}
