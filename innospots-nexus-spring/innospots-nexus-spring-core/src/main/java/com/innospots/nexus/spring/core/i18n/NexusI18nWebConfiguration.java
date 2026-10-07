package com.innospots.nexus.spring.core.i18n;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Servlet Web 场景下将 Spring locale 同步到 {@link com.innospots.nexus.base.i18n.I18nConverter}。
 *
 * @author Smars
 * @date 2026/09/16
 * @see EnableNexusI18n
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.filter.OncePerRequestFilter")
@ConditionalOnProperty(prefix = "nexus.i18n", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(prefix = "nexus.i18n", name = "sync-web-locale", havingValue = "true", matchIfMissing = true)
public class NexusI18nWebConfiguration {

    @Bean
    FilterRegistrationBean<NexusI18nLocaleFilter> nexusI18nLocaleFilterRegistration() {
        FilterRegistrationBean<NexusI18nLocaleFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new NexusI18nLocaleFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.setName("nexusI18nLocaleFilter");
        return registration;
    }
}
