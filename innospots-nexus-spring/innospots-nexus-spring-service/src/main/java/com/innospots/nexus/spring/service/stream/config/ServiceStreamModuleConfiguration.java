package com.innospots.nexus.spring.service.stream.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.core.ServiceCoreConfiguration;
import com.innospots.nexus.spring.service.stream.mvc.ServiceMvcStreamConfiguration;
import com.innospots.nexus.spring.service.stream.webflux.ServiceWebFluxStreamConfiguration;

/**
 * 流式（SSE / {@code StreamSession}）Spring 装配，按 Web 类型二选一生效。
 *
 * @see EnableNexusServiceStream
 */
public final class ServiceStreamModuleConfiguration {

    private ServiceStreamModuleConfiguration() {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Import({
            ServiceCoreConfiguration.class,
            ServiceStreamConfiguration.class,
            ServiceMvcStreamConfiguration.class
    })
    public static class ServletStreamModuleConfiguration {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.web.reactive.DispatcherHandler")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Import({
            ServiceCoreConfiguration.class,
            ServiceStreamConfiguration.class,
            ServiceWebFluxStreamConfiguration.class
    })
    public static class ReactiveStreamModuleConfiguration {
    }
}
