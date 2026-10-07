package com.innospots.nexus.spring.service.stream.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.session.DefaultStreamManager;
import com.innospots.nexus.service.stream.session.StreamManager;

/**
 * 中立流运行时 Bean（Spring 与 WebFlux 共享）。
 */
@Configuration
public class ServiceStreamConfiguration {

    @Bean
    @ConditionalOnMissingBean
    StreamConfig serviceStreamConfig() {
        return StreamConfig.defaults();
    }

    @Bean
    @ConditionalOnMissingBean
    StreamManager serviceStreamManager(
            StreamConfig serviceStreamConfig,
            ThreadBoundServiceContext serviceThreadBoundServiceContext) {
        return DefaultStreamManager.builder()
                .config(serviceStreamConfig)
                .contexts(serviceThreadBoundServiceContext)
                .build();
    }
}
