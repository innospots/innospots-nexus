package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributionCatalog;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionDecoder;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.console.ConsolePluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.contribution.console.ReservedPluginResourceCatalog;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;

/**
 * Console 插件 Contribution 三连 CDI 生产者。
 *
 * <p>为 app 模块的 {@link PluginHostLifecycle} 提供 Decoder、Handler 与 Snapshotter Bean。</p>
 */
@ApplicationScoped
public class PluginContributionBeans {

    /**
     * Console 活动贡献目录 Bean。
     *
     * <p>插件 ACTIVE 后由 Handler 写入已发布菜单与页面。</p>
     */
    @Produces
    @Singleton
    ConsoleContributionCatalog consoleContributionCatalog() {
        return new ConsoleContributionCatalog();
    }

    /**
     * 平台保留资源目录 Bean。
     *
     * <p>校验插件是否占用宿主保留的 module/page 身份。</p>
     */
    @Produces
    @Singleton
    ReservedPluginResourceCatalog reservedPluginResourceCatalog() {
        return new ReservedPluginResourceCatalog(List.of());
    }

    /**
     * YAML Contribution 解码注册表 Bean。
     *
     * <p>注册 {@link ConsolePluginContributionDecoder} 解析 {@code console@1}。</p>
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
     *
     * <p>注册 {@link ConsolePluginContributionSnapshotter} 生成安装表摘要。</p>
     */
    @Produces
    @Singleton
    PluginContributionSnapshotterRegistry pluginContributionSnapshotterRegistry() {
        return PluginContributionSnapshotterRegistry.builder()
                .register(new ConsolePluginContributionSnapshotter())
                .build();
    }

    /**
     * 运行时 Contribution 处理器列表 Bean。
     *
     * @param consoleContributionCatalog    活动贡献目录
     * @param reservedPluginResourceCatalog 保留资源目录
     * @return 含 Console Handler 的列表
     */
    @Produces
    @Singleton
    List<PluginContributionHandler<?>> pluginContributionHandlers(
            ConsoleContributionCatalog consoleContributionCatalog,
            ReservedPluginResourceCatalog reservedPluginResourceCatalog) {
        return List.of(new ConsolePluginContributionHandler(
                consoleContributionCatalog,
                reservedPluginResourceCatalog));
    }
}
