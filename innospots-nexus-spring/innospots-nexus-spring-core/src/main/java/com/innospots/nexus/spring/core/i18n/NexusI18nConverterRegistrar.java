package com.innospots.nexus.spring.core.i18n;

import com.innospots.nexus.base.i18n.I18nConverter;
import com.innospots.nexus.base.i18n.I18nMessageResolver;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;

/**
 * 在 Spring 容器启动后将 {@link I18nMessageResolver} 注册到 {@link I18nConverter}。
 *
 * @author Smars
 * @date 2026/09/16
 */
public class NexusI18nConverterRegistrar {

    private final I18nMessageResolver messageResolver;

    public NexusI18nConverterRegistrar(ObjectProvider<I18nMessageResolver> messageResolverProvider) {
        this.messageResolver = messageResolverProvider.getIfAvailable();
    }

    @PostConstruct
    void registerMessageResolver() {
        if (messageResolver != null) {
            I18nConverter.setMessageResolver(messageResolver);
        }
    }
}
