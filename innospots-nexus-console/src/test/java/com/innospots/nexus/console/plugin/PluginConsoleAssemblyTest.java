package com.innospots.nexus.console.plugin;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.console.plugin.contribution.ConsoleContributionCatalog;
import com.innospots.nexus.console.plugin.contribution.ConsoleModuleDeclaration;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContribution;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionHandler;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.console.plugin.contribution.MenuDeclaration;
import com.innospots.nexus.console.plugin.contribution.ReservedPluginResourceCatalog;
import com.innospots.nexus.console.plugin.contribution.UiSpecPageDeclaration;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contract.Plugin;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.discovery.DiscoveredPlugin;
import com.innospots.nexus.core.plugin.discovery.PluginCatalog;
import com.innospots.nexus.core.plugin.installation.service.PluginRuntimeFactory;
import com.innospots.nexus.core.plugin.runtime.PluginManager;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证 Console Contribution 与 Core PluginManager 使用同一生命周期。 */
class PluginConsoleAssemblyTest {

    /** 验证 Contribution 只在统一运行时可用后发布，停止后同步撤出。 */
    @Test
    void sharesAvailabilityWithCorePluginRuntime() {
        ConsoleContributionCatalog contributionCatalog = new ConsoleContributionCatalog();
        ConsolePluginContributionHandler handler = new ConsolePluginContributionHandler(
                contributionCatalog,
                new ReservedPluginResourceCatalog(List.of()));
        PluginContributionSnapshotterRegistry snapshotters = PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
        Plugin plugin = new ConsolePlugin();
        PluginCatalog catalog = PluginCatalog.of(List.of(
                new DiscoveredPlugin(plugin, plugin.definition(), Instant.now())));
        PluginRuntimeFactory factory = new PluginRuntimeFactory(
                new com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig(
                        Set.of(), Set.of(), java.util.Map.of(), java.util.Map.of(), java.util.Map.of(), null),
                List.of(handler),
                snapshotters);

        PluginManager manager = factory.create(catalog, Set.of("com.example.console"));
        manager.start();
        assertThat(contributionCatalog.activeContributions()).hasSize(1);

        manager.stop("com.example.console");
        assertThat(contributionCatalog.activeContributions()).isEmpty();
        manager.close();
    }

    /** Console 装配测试使用的最小插件定义。 */
    private static final class ConsolePlugin implements Plugin {

        @Override
        public PluginDefinition definition() {
            return PluginDefinition.builder("com.example.console")
                    .name("Console Plugin")
                    .version("1.0.0")
                    .contribute(new ConsolePluginContribution(List.of(
                            new ConsoleModuleDeclaration(
                                    "sales",
                                    I18nObject.of("en", "Sales"),
                                    null,
                                    List.of(new UiSpecPageDeclaration("home", "/sales", List.of())),
                                    List.of(MenuDeclaration.page(
                                            "home", I18nObject.of("en", "Home"), null, 0, "home"))))))
                    .build();
        }
    }
}
