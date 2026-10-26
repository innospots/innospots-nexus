package com.innospots.nexus.spring.console.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.i18n.endpoint.I18nBundleEndpoint;
import com.innospots.nexus.console.i18n.loader.MessageBundleLoader;
import com.innospots.nexus.console.i18n.service.MessageBundleService;

/**
 * Console 语言包读取与公共 REST 资源的 Spring 装配。
 *
 * @author Smars
 * @date 2026/10/26
 */
@Configuration
public class ConsoleI18nConfiguration {

    @Bean
    MessageBundleLoader messageBundleLoader() {
        return new MessageBundleLoader();
    }

    @Bean
    MessageBundleService messageBundleService(MessageBundleLoader loader) {
        return new MessageBundleService(loader);
    }

    @Bean
    I18nBundleEndpoint i18nBundleEndpoint(MessageBundleService service) {
        return new I18nBundleEndpoint(service);
    }
}
