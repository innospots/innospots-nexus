package com.innospots.nexus.spring.plugin.config;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

/**
 * 在 Spring 容器就绪后的 {@link ApplicationRunner} 阶段启用插件子系统。
 *
 * <p>避免在 Bean 初始化阶段调用 {@link PluginHostBootstrap#enable}，
 * 确保数据源、MyBatis 等基础设施已装配完成。</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PluginHostBootstrapRunner implements ApplicationRunner {

    private final PluginInstallationDao installationDao;
    private final PluginRuntimeConfig runtimeConfig;
    private final PluginHostProperties properties;
    private final ObjectProvider<PluginContributionDecoderRegistry> contributionDecoders;
    private final List<PluginContributionHandler<?>> contributionHandlers;
    private final ObjectProvider<PluginContributionSnapshotterRegistry> contributionSnapshotters;
    private final PluginInstallationManagerHolder managerHolder;

    /**
     * @param installationDao          安装表 DAO
     * @param runtimeConfig            插件运行时配置
     * @param properties               宿主安装/启停策略
     * @param contributionDecoders     可选 YAML Contribution 解码表
     * @param contributionHandlers     运行时 Contribution 处理器列表
     * @param contributionSnapshotters 可选对账快照序列化表
     * @param managerHolder            安装管理器持有器
     */
    public PluginHostBootstrapRunner(
            PluginInstallationDao installationDao,
            PluginRuntimeConfig runtimeConfig,
            PluginHostProperties properties,
            ObjectProvider<PluginContributionDecoderRegistry> contributionDecoders,
            List<PluginContributionHandler<?>> contributionHandlers,
            ObjectProvider<PluginContributionSnapshotterRegistry> contributionSnapshotters,
            PluginInstallationManagerHolder managerHolder) {
        this.installationDao = installationDao;
        this.runtimeConfig = runtimeConfig;
        this.properties = properties;
        this.contributionDecoders = contributionDecoders;
        this.contributionHandlers = contributionHandlers;
        this.contributionSnapshotters = contributionSnapshotters;
        this.managerHolder = managerHolder;
    }

    /**
     * 容器刷新完成后启用插件子系统。
     *
     * @param args 应用启动参数（未使用）
     */
    @Override
    public void run(ApplicationArguments args) {
        // console 模块未装配时使用空表，仍允许纯 Java SPI 插件运行
        PluginContributionDecoderRegistry decoderRegistry = contributionDecoders.getIfAvailable(
                () -> PluginContributionDecoderRegistry.builder().build());
        PluginContributionSnapshotterRegistry snapshotterRegistry = contributionSnapshotters.getIfAvailable(
                () -> PluginContributionSnapshotterRegistry.builder().build());
        PluginInstallationConfig installationConfig =
                new PluginInstallationConfig(properties.getPlugin().isAutoInstall());
        PluginInstallationManager manager = PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfig,
                installationConfig,
                decoderRegistry,
                contributionHandlers,
                snapshotterRegistry,
                null));
        managerHolder.setManager(manager);
    }
}
