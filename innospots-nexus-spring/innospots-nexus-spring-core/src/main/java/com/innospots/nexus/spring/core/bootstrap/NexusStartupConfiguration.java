package com.innospots.nexus.spring.core.bootstrap;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.innospots.nexus.core.bootstrap.NexusStartup;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 应用服务启动编排装配。
 *
 * <p>启动任务在 {@link ApplicationRunner} 执行期收集，避免 {@code @ConditionalOnBean} 早于
 * 各域 {@link NexusStartupTask} 注册而导致管线被跳过。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Configuration
public class NexusStartupConfiguration {

    /**
     * 容器就绪后执行启动编排。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ApplicationRunner nexusStartupRunner(ObjectProvider<List<NexusStartupTask>> startupTasksProvider) {
        return args -> {
            List<NexusStartupTask> startupTasks = startupTasksProvider.getIfAvailable(List::of);
            if (startupTasks.isEmpty()) {
                return;
            }
            NexusStartup.Builder builder = NexusStartup.builder();
            for (NexusStartupTask task : startupTasks) {
                builder.task(task);
            }
            builder.build().run();
        };
    }
}
