package com.innospots.nexus.spring.core.bootstrap;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.innospots.nexus.core.bootstrap.NexusStartup;
import com.innospots.nexus.core.bootstrap.NexusStartupTask;

/**
 * 应用服务启动编排装配。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Configuration
public class NexusStartupConfiguration {

    /**
     * 组装完整启动管线；插件、console 等模块通过 {@link NexusStartupTask} Bean 扩展。
     */
    @Bean
    @ConditionalOnBean(NexusStartupTask.class)
    NexusStartup nexusStartup(List<NexusStartupTask> startupTasks) {
        NexusStartup.Builder builder = NexusStartup.builder();
        for (NexusStartupTask task : startupTasks) {
            builder.task(task);
        }
        return builder.build();
    }

    /**
     * 容器就绪后执行启动编排。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnBean(NexusStartup.class)
    ApplicationRunner nexusStartupRunner(NexusStartup nexusStartup) {
        return args -> nexusStartup.run();
    }
}
