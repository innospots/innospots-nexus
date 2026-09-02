package com.innospots.nexus.core.plugin.installation;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.installation.service.PluginRuntimeFactory;
import com.innospots.nexus.core.plugin.lifecycle.PluginState;
import com.innospots.nexus.core.plugin.runtime.PluginManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Core 宿主装配契约只创建统一 PluginManager。 */
class PluginHostAssemblyTest {

    /** 验证有效目录中的 eligible 插件通过同一个运行时进入 ACTIVE。 */
    @Test
    void createsOneRuntimeForEligibleCatalogPlugins() {
        Plugin plugin = new HostPlugin();
        PluginCatalog catalog = PluginCatalog.of(List.of(
                new DiscoveredPlugin(plugin, plugin.definition(), Instant.now())));
        PluginRuntimeFactory factory = new PluginRuntimeFactory(
                new PluginRuntimeConfig(Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), null),
                List.of(),
                PluginContributionSnapshotterRegistry.builder().build());

        PluginManager manager = factory.create(catalog, Set.of("com.example.host"));
        manager.start();

        assertThat(manager.plugin("com.example.host").orElseThrow().state())
                .isEqualTo(PluginState.ACTIVE);

        manager.close();
    }

    /** 宿主装配测试使用的最小 Java Plugin。 */
    private static final class HostPlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.host")
                    .name("Host Plugin")
                    .version("1.0.0")
                    .build();
        }
    }
}
