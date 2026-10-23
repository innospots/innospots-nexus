package com.innospots.nexus.spring.console.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

import com.innospots.nexus.core.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration;

/**
 * 启动时扫描 Spring 容器中声明了 {@link com.innospots.nexus.core.endpoint.grant.EndpointModule}
 * 的 JAX-RS 资源 Bean（本类或 {@code implements} 的 console 契约接口）并填充 {@link EndpointGrantManager}。
 *
 * <p>与 Jersey 注册条件（类上 {@link jakarta.ws.rs.Path}）解耦：grant 目录以模块归属为准；
 * 路径仍从运行时资源类解析（实现类 {@code @Path} + 契约方法路径）。</p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ConsoleEndpointGrantConfiguration {

    @Bean
    @ConditionalOnMissingBean
    EndpointGrantManager endpointGrantManager() {
        return new EndpointGrantManager();
    }

    @Bean
    ApplicationListener<ContextRefreshedEvent> endpointGrantResourceRegistration(EndpointGrantManager manager) {
        return event -> {
            ConfigurableListableBeanFactory beanFactory =
                    (ConfigurableListableBeanFactory) event.getApplicationContext().getAutowireCapableBeanFactory();
            for (String beanName : beanFactory.getBeanDefinitionNames()) {
                Class<?> resourceClass = NexusJaxRsConfiguration.resolveEndpointClass(beanFactory, beanName);
                if (resourceClass == null) {
                    continue;
                }
                if (EndpointGrantAnnotations.isGrantResourceType(resourceClass)) {
                    manager.registerResource(resourceClass);
                }
            }
        };
    }
}
