package com.innospots.nexus.spring.console.role.support;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration;

/**
 * 角色 HTTP 集成测试用 Jersey 装配：注册 Spring 管理的端点实例。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RoleEndpointJerseyIntegrationConfiguration {

    @Bean
    ResourceConfig roleEndpointJerseyResourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        return resourceConfig;
    }

    @Bean
    ResourceConfigCustomizer roleEndpointJerseyRegistrationCustomizer(ApplicationContext applicationContext) {
        return resourceConfig -> NexusJaxRsConfiguration.registerPathAnnotatedEndpoints(applicationContext, resourceConfig);
    }
}
