package com.innospots.nexus.spring.console.config;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantManager;
import com.innospots.nexus.console.endpoint.grant.support.EndpointGrantAnnotations;

/**
 * 启动时扫描 Spring 容器中声明了 {@link com.innospots.nexus.console.endpoint.grant.EndpointModule}
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
            var context = event.getApplicationContext();
            for (String beanName : context.getBeanDefinitionNames()) {
                Class<?> type = context.getType(beanName);
                if (type == null) {
                    continue;
                }
                Class<?> resourceClass = AopUtils.getTargetClass(type);
                if (EndpointGrantAnnotations.isGrantResourceType(resourceClass)) {
                    manager.registerResource(resourceClass);
                }
            }
        };
    }
}
