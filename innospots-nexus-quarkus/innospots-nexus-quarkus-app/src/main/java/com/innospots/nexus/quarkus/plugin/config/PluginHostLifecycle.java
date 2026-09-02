package com.innospots.nexus.quarkus.plugin.config;

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrap;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * 插件子系统 Quarkus 生命周期监听器。
 *
 * <p>在基础设施（数据源、MyBatis 等）就绪后的 {@link StartupEvent} 中调用
 * {@link PluginHostBootstrap#enable(com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest)}，
 * 避免在 CDI 构造阶段过早启用插件。</p>
 */
@ApplicationScoped
public class PluginHostLifecycle {

    private final PluginInstallationDao installationDao;
    private final PluginRuntimeConfigFactory runtimeConfigFactory;
    private final NexusPluginHostConfig hostConfig;
    private final Instance<PluginContributionDecoderRegistry> decoders;
    private final Instance<PluginContributionSnapshotterRegistry> snapshotters;
    private final Instance<PluginContributionHandler<?>> handlers;
    private final PluginInstallationManagerHolder managerHolder;

    /**
     * @param installationDao       安装表 DAO
     * @param runtimeConfigFactory  运行时配置工厂
     * @param hostConfig            宿主策略配置
     * @param decoders              可选 Contribution 解码表；console 模块提供
     * @param snapshotters          可选对账快照表；console 模块提供
     * @param handlers              可选 Contribution 处理器；console 模块提供
     * @param managerHolder         安装管理器持有器
     */
    @Inject
    public PluginHostLifecycle(
            PluginInstallationDao installationDao,
            PluginRuntimeConfigFactory runtimeConfigFactory,
            NexusPluginHostConfig hostConfig,
            Instance<PluginContributionDecoderRegistry> decoders,
            Instance<PluginContributionSnapshotterRegistry> snapshotters,
            Instance<PluginContributionHandler<?>> handlers,
            PluginInstallationManagerHolder managerHolder) {
        this.installationDao = installationDao;
        this.runtimeConfigFactory = runtimeConfigFactory;
        this.hostConfig = hostConfig;
        this.decoders = decoders;
        this.snapshotters = snapshotters;
        this.handlers = handlers;
        this.managerHolder = managerHolder;
    }

    /**
     * 应用启动完成后启用插件子系统。
     *
     * @param event Quarkus 启动事件（未使用，仅作触发）
     */
    void onStart(@Observes @Priority(100) StartupEvent event) {
        // console 模块未引入时使用空注册表，兼容纯 SPI 插件
        PluginContributionDecoderRegistry decoderRegistry = decoders.isResolvable()
                ? decoders.get()
                : PluginContributionDecoderRegistry.builder().build();
        PluginContributionSnapshotterRegistry snapshotterRegistry = snapshotters.isResolvable()
                ? snapshotters.get()
                : PluginContributionSnapshotterRegistry.builder().build();
        List<PluginContributionHandler<?>> handlerList = handlers.stream().toList();
        PluginInstallationManager manager = PluginHostBootstrap.enable(new PluginHostBootstrapRequest(
                installationDao,
                runtimeConfigFactory.create(),
                new PluginInstallationConfig(hostConfig.autoInstall()),
                decoderRegistry,
                handlerList,
                snapshotterRegistry,
                // 使用 TCCL 扫描 classpath 上的 SPI 与 plugin.yaml
                Thread.currentThread().getContextClassLoader()));
        managerHolder.setManager(manager);
    }

    /**
     * 应用关闭时按逆序释放插件运行时。
     *
     * @param event Quarkus 关闭事件（未使用，仅作触发）
     */
    void onStop(@Observes @Priority(100) ShutdownEvent event) {
        PluginInstallationManager manager = managerHolder.managerIfPresent();
        if (manager != null) {
            manager.close();
        }
        managerHolder.clearManager();
    }

    /**
     * 返回已启用的安装管理器，供业务 Service 查询 Capability。
     *
     * @return 安装管理器
     * @throws IllegalStateException 插件子系统尚未启动时
     */
    public PluginInstallationManager installationManager() {
        return managerHolder.requireManager();
    }
}
