package com.innospots.nexus.spring.plugin.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PluginHostConfigBinder} 行为验证。
 */
class PluginHostConfigBinderTest {

    /**
     * 验证仅收集 {@code plugins.*} 键，忽略 {@code nexus.*} 宿主策略键。
     */
    @Test
    void flattensPluginPrefixedProperties() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("plugins.com.example.message-wecom.corpId", "ww-demo");
        environment.setProperty("nexus.plugin.auto-install", "false");

        assertThat(PluginHostConfigBinder.flattenPluginConfig(environment))
                .containsEntry("plugins.com.example.message-wecom.corpId", "ww-demo")
                .doesNotContainKey("nexus.plugin.auto-install");
    }
}
