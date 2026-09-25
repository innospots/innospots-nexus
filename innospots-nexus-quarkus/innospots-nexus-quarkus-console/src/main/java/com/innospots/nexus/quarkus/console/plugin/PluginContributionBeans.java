package com.innospots.nexus.quarkus.console.plugin;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.quarkus.plugin.config.PluginContributionHandlers;
import com.innospots.nexus.quarkus.plugin.config.PluginHostLifecycle;

/**
 * Console 插件 Contribution 三连 CDI 生产者。
 *
 * <p>为 {@link PluginHostLifecycle} 提供 Decoder、Handler 与 Snapshotter Bean。</p>
 */
@ApplicationScoped
public class PluginContributionBeans {

    /**
     * Console 活动贡献目录 Bean。
     */
    @Produces
    @Singleton
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    /**
     * 平台保留资源目录 Bean。
     */
    @Produces
    @Singleton
    ReservedPluginResourceCatalog reservedPluginResourceCatalog() {
        return new ReservedPluginResourceCatalog(List.of());
    }

    /**
     * YAML Contribution 解码注册表 Bean。
     */
    @Produces
    @Singleton
    PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
        return PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
    }

    /**
     * 对账快照注册表 Bean。
     */
    @Produces
    @Singleton
    PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    /**
     * 运行时 Contribution 处理器集合 Bean。
     */
    @Produces
    @Singleton
    PluginContributionHandlers pluginContributionHandlers(
            ConsoleContributionCatalog consoleContributionCatalog,
            ReservedPluginResourceCatalog reservedPluginResourceCatalog) {
        return new PluginContributionHandlers(List.of(new ConsolePluginContributionHandler(
                consoleContributionCatalog,
                reservedPluginResourceCatalog)));
    }
}
