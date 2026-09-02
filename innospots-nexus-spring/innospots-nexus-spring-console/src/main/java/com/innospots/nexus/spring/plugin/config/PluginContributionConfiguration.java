package com.innospots.nexus.spring.plugin.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.plugin.contribution.ConsoleContributionCatalog;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionDecoder;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionHandler;
import com.innospots.nexus.console.plugin.contribution.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.console.plugin.contribution.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;

/**
 * Console 插件 Contribution 三连装配。
 *
 * <p>为 YAML {@code console@1} 贡献注册 Decoder、Handler 与 Snapshotter，
 * 并在插件 ACTIVE 后将菜单/页面发布到 {@link ConsoleContributionCatalog}。</p>
 *
 * @see PluginConsoleConfiguration
 */
@Configuration
public class PluginContributionConfiguration {

    /**
     * Console 活动贡献目录。
     *
     * <p>Handler commit 后写入；前端路由与权限同步从此目录读取已发布资源。</p>
     */
    @Bean
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    /**
     * 平台保留资源目录。
     *
     * <p>声明宿主保留的 module/page 身份，防止插件占用冲突路径。</p>
     */
    @Bean
    ReservedPluginResourceCatalog reservedPluginResourceCatalog() {
        return new ReservedPluginResourceCatalog(List.of());
    }

    /**
     * YAML Contribution 解码注册表。
     *
     * <p>发现阶段将 {@code console@1} 段解码为 {@code ConsolePluginContribution}。</p>
     */
    @Bean
    PluginContributionDecoderRegistry pluginContributionDecoderRegistry() {
        return PluginContributionDecoderRegistry.builder()
                .register(new ConsolePluginContributionDecoder())
                .build();
    }

    /**
     * 对账阶段 Contribution 快照注册表。
     *
     * <p>将 console contribution 序列化为安装表 {@code definition_snapshot} 字段。</p>
     */
    @Bean
    PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    /**
     * 运行时 Contribution 处理器列表。
     *
     * <p>插件 {@code start()} 时执行 stage/commit，停止时从 catalog 撤出贡献。</p>
     *
     * @param consoleContributionCatalog   活动贡献目录
     * @param reservedPluginResourceCatalog 保留资源校验目录
     * @return 含 Console Handler 的不可变列表
     */
    @Bean
    List<PluginContributionHandler<?>> pluginContributionHandlers(
            ConsoleContributionCatalog consoleContributionCatalog,
            ReservedPluginResourceCatalog reservedPluginResourceCatalog) {
        return List.of(new ConsolePluginContributionHandler(
                consoleContributionCatalog,
                reservedPluginResourceCatalog));
    }
}
