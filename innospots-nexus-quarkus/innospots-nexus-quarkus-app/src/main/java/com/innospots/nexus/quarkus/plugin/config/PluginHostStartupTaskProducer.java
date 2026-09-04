package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;
import java.util.function.Consumer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.innospots.nexus.core.bootstrap.PluginHostStartupTask;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

import io.quarkus.arc.All;

/**
 * 插件宿主启动任务 Bean 生产器。
 */
@ApplicationScoped
public class PluginHostStartupTaskProducer {

    private final PluginInstallationDao installationDao;
    private final PluginRuntimeConfigFactory runtimeConfigFactory;
    private final NexusPluginHostConfig hostConfig;
    private final Instance<PluginContributionDecoderRegistry> decoders;
    private final Instance<PluginContributionSnapshotterRegistry> snapshotters;
    private final List<PluginContributionHandler<?>> contributionHandlers;
    private final PluginInstallationManagerHolder managerHolder;

    /**
     * @param installationDao       安装表 DAO
     * @param runtimeConfigFactory  运行时配置工厂
     * @param hostConfig            宿主策略配置
     * @param decoders              可选 Contribution 解码表
     * @param snapshotters          可选对账快照表
     * @param handlers              可选 Contribution 处理器
     * @param managerHolder         安装管理器持有器
     */
    @Inject
    public PluginHostStartupTaskProducer(
            PluginInstallationDao installationDao,
            PluginRuntimeConfigFactory runtimeConfigFactory,
            NexusPluginHostConfig hostConfig,
            Instance<PluginContributionDecoderRegistry> decoders,
            Instance<PluginContributionSnapshotterRegistry> snapshotters,
            @All List<PluginContributionHandler<?>> handlers,
            PluginInstallationManagerHolder managerHolder) {
        this.installationDao = installationDao;
        this.runtimeConfigFactory = runtimeConfigFactory;
        this.hostConfig = hostConfig;
        this.decoders = decoders;
        this.snapshotters = snapshotters;
        this.contributionHandlers = handlers;
        this.managerHolder = managerHolder;
    }

    /**
     * 创建插件宿主启动任务。
     */
    @Produces
    @ApplicationScoped
    PluginHostStartupTask pluginHostStartupTask() {
        Consumer<PluginInstallationManager> managerConsumer = managerHolder::setManager;
        return new PluginHostStartupTask(this::bootstrapRequest, managerConsumer);
    }

    private PluginHostBootstrapRequest bootstrapRequest() {
        PluginContributionDecoderRegistry decoderRegistry = decoders.isResolvable()
                ? decoders.get()
                : PluginContributionDecoderRegistry.builder().build();
        PluginContributionSnapshotterRegistry snapshotterRegistry = snapshotters.isResolvable()
                ? snapshotters.get()
                : PluginContributionSnapshotterRegistry.builder().build();
        PluginRuntimeConfig runtimeConfig = runtimeConfigFactory.create();
        return new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfig,
                new PluginInstallationConfig(hostConfig.autoInstall()),
                decoderRegistry,
                contributionHandlers,
                snapshotterRegistry,
                Thread.currentThread().getContextClassLoader());
    }
}
