package com.innospots.nexus.spring.console.jaxrs;

import jakarta.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将 Spring 容器中带 {@link Path} 的 Bean 注册为 Jersey 资源，直接对外暴露 Jakarta REST 契约。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class NexusJaxRsConfiguration {

    @Bean
    ResourceConfig nexusJaxRsResourceConfig(List<NexusJerseyResourceConfigurer> jerseyResourceConfigurers) {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(SpringComponentProvider.class);
        resourceConfig.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        for (NexusJerseyResourceConfigurer configurer : jerseyResourceConfigurers) {
            configurer.configure(resourceConfig);
        }
        return resourceConfig;
    }

    /**
     * 在 {@link ResourceConfig} 基础 Bean 创建之后注册带 {@link Path} 的 Spring Bean 类型。
     *
     * <p>仅解析 Bean 定义并 {@code register(Class)}，不调用 {@code getBeansWithAnnotation}，
     * 以免 Jersey 启动阶段提前实例化带 {@code @Lazy} 的端点（如插件管理 REST）。</p>
     */
    @Bean
    ResourceConfigCustomizer nexusJaxRsEndpointCustomizer(ApplicationContext applicationContext) {
        return resourceConfig -> {
            for (String beanName : applicationContext.getBeanNamesForAnnotation(Path.class)) {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType == null) {
                    continue;
                }
                Class<?> resourceClass = AopUtils.getTargetClass(beanType);
                resourceConfig.register(resourceClass);
            }
        };
    }
}
