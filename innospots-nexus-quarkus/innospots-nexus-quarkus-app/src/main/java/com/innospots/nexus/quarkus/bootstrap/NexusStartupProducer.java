package com.innospots.nexus.quarkus.bootstrap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.innospots.nexus.core.bootstrap.NexusStartup;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;
import com.innospots.nexus.core.bootstrap.PluginHostStartupTask;

/**
 * Quarkus 启动编排 Bean 生产器。
 */
@ApplicationScoped
public class NexusStartupProducer {

    private final PluginHostStartupTask pluginHostStartupTask;
    private final Instance<NexusStartupTask> startupTasks;

    /**
     * @param pluginHostStartupTask 插件宿主启动任务
     * @param startupTasks          其他启动任务
     */
    @Inject
    public NexusStartupProducer(
            PluginHostStartupTask pluginHostStartupTask,
            Instance<NexusStartupTask> startupTasks) {
        this.pluginHostStartupTask = pluginHostStartupTask;
        this.startupTasks = startupTasks;
    }

    /**
     * 组装完整启动管线。
     */
    @Produces
    @ApplicationScoped
    NexusStartup nexusStartup() {
        NexusStartup.Builder builder = NexusStartup.builder().task(pluginHostStartupTask);
        for (NexusStartupTask task : startupTasks) {
            if (task != pluginHostStartupTask) {
                builder.task(task);
            }
        }
        return builder.build();
    }
}
