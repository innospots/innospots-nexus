package com.innospots.nexus.spring.core.plugin;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.plugin.bootstrap.PluginHostStartupTask;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;

/**
 * 插件宿主启动任务 Spring 装配。
 *
 * <p>由 {@link EnableNexusPluginHost} 显式引入，注册 {@link PluginHostStartupTask} 供
 * {@link com.innospots.nexus.spring.core.bootstrap.NexusStartupConfiguration} 汇总。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Configuration
public class PluginHostStartupConfiguration {

    /**
     * 内置插件宿主启动任务。
     */
    @Bean
    PluginHostStartupTask pluginHostStartupTask(
            ObjectProvider<PluginInstallationDao> installationDao,
            PluginRuntimeConfig runtimeConfig,
            PluginHostProperties properties,
            ObjectProvider<PluginContributionDecoderRegistry> contributionDecoders,
            List<PluginContributionHandler<?>> contributionHandlers,
            ObjectProvider<PluginContributionSnapshotterRegistry> contributionSnapshotters,
            PluginInstallationManagerHolder managerHolder) {
        return new PluginHostStartupTask(
                () -> new PluginHostBootstrapRequest(
                        installationDao.getObject(),
                        runtimeConfig,
                        new PluginInstallationConfig(properties.getPlugin().isAutoInstall()),
                        contributionDecoders.getIfAvailable(
                                () -> PluginContributionDecoderRegistry.builder().build()),
                        contributionHandlers,
                        contributionSnapshotters.getIfAvailable(
                                () -> PluginContributionSnapshotterRegistry.builder().build()),
                        null),
                managerHolder::setManager);
    }
}
