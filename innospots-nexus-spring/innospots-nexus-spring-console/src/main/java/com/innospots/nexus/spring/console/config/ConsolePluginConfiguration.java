package com.innospots.nexus.spring.console.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.endpoint.PluginManagementEndpoint;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.spring.core.plugin.PluginInstallationManagerHolder;

/**
 * {@code console.plugin} 域 Spring 装配：Contribution 编解码与插件管理 REST。
 *
 * @author Smars
 * @date 2026/09/23
 */
@Configuration
public class ConsolePluginConfiguration {

    @Bean
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    @Bean
    ReservedPluginResourceCatalog reservedPluginResourceCatalog() {
        return new ReservedPluginResourceCatalog(List.of());
    }

    @Bean
    PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
        return PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
    }

    @Bean
    PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    @Bean
    List<PluginContributionHandler<?>> pluginContributionHandlers(
            ConsoleContributionCatalog consoleContributionCatalog,
            ReservedPluginResourceCatalog reservedPluginResourceCatalog) {
        return List.of(new ConsolePluginContributionHandler(
                consoleContributionCatalog,
                reservedPluginResourceCatalog));
    }

    @Bean
    @Lazy
    PluginManagementEndpoint pluginManagementEndpoint(
            PluginInstallationManagerHolder managerHolder,
            ConsoleCatalogSyncService syncService) {
        return new PluginManagementEndpoint(
                managerHolder::requireManager,
                PluginManagementConverter.INSTANCE,
                syncService);
    }
}
