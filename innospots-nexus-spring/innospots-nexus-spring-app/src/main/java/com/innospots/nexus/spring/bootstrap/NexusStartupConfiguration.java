package com.innospots.nexus.spring.bootstrap;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.innospots.nexus.core.bootstrap.NexusStartup;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.bootstrap.PluginHostStartupTask;
import com.innospots.nexus.core.plugin.contribution.PluginContributionDecoderRegistry;
import com.innospots.nexus.core.plugin.contribution.PluginContributionHandler;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.installation.bootstrap.PluginHostBootstrapRequest;
import com.innospots.nexus.core.plugin.installation.config.PluginInstallationConfig;
import com.innospots.nexus.core.plugin.installation.dao.PluginInstallationDao;
import com.innospots.nexus.core.plugin.runtime.PluginRuntimeConfig;
import com.innospots.nexus.spring.plugin.config.PluginHostProperties;
import com.innospots.nexus.spring.plugin.config.PluginInstallationManagerHolder;

/**
 * 应用服务启动编排装配。
 */
@Configuration
public class NexusStartupConfiguration {

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

    /**
     * 组装完整启动管线；console 等模块通过额外 {@link NexusStartupTask} Bean 扩展。
     */
    @Bean
    NexusStartup nexusStartup(
            PluginHostStartupTask pluginHostStartupTask,
            List<NexusStartupTask> startupTasks) {
        NexusStartup.Builder builder = NexusStartup.builder().task(pluginHostStartupTask);
        for (NexusStartupTask task : startupTasks) {
            if (task != pluginHostStartupTask) {
                builder.task(task);
            }
        }
        return builder.build();
    }

    /**
     * 容器就绪后执行启动编排。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ApplicationRunner nexusStartupRunner(NexusStartup nexusStartup) {
        return args -> nexusStartup.run();
    }
}
