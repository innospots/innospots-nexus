package com.innospots.nexus.spring.service.http.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.core.ServiceCoreConfiguration;
import com.innospots.nexus.spring.service.core.ServiceProperties;
import com.innospots.nexus.spring.service.http.webflux.ServiceWebFluxConfiguration;

/**
 * 中立运行时与 Spring WebFlux 装配。
 *
 * @see EnableNexusServiceHttp
 */
@Configuration
@ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ServiceProperties.class)
@ConditionalOnClass(name = "org.springframework.web.reactive.DispatcherHandler")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Import({
        ServiceCoreConfiguration.class,
        ServiceWebFluxConfiguration.class
})
public class ServiceReactiveConfiguration {
}
