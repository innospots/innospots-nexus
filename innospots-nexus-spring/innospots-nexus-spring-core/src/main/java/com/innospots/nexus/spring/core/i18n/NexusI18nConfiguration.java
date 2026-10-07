package com.innospots.nexus.spring.core.i18n;

import com.innospots.nexus.base.i18n.I18nMessageResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * 将 Spring ResourceBundle / {@link MessageSource} 桥接到 {@link com.innospots.nexus.base.i18n.I18nConverter}。
 *
 * @author Smars
 * @date 2026/09/16
 * @see EnableNexusI18n
 */
@Configuration
@ConditionalOnClass({MessageSource.class, I18nMessageResolver.class})
@EnableConfigurationProperties(NexusI18nProperties.class)
@ConditionalOnProperty(prefix = "nexus.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NexusI18nConfiguration {

    @Bean
    @ConditionalOnMissingBean(MessageSource.class)
    MessageSource nexusMessageSource(NexusI18nProperties properties) {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(properties.getBasenames().toArray(String[]::new));
        messageSource.setDefaultEncoding(properties.getEncoding());
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    @ConditionalOnMissingBean(I18nMessageResolver.class)
    MessageSourceI18nMessageResolver nexusMessageSourceI18nMessageResolver(MessageSource messageSource) {
        return new MessageSourceI18nMessageResolver(messageSource);
    }

    @Bean
    NexusI18nConverterRegistrar nexusI18nConverterRegistrar(
            org.springframework.beans.factory.ObjectProvider<I18nMessageResolver> resolverProvider
    ) {
        return new NexusI18nConverterRegistrar(resolverProvider);
    }
}
