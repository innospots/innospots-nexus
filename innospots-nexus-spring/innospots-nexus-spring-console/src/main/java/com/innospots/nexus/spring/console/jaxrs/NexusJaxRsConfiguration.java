package com.innospots.nexus.spring.console.jaxrs;

import jakarta.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 Spring 容器中带 {@link Path} 的 Bean 注册为 Jersey 资源，直接对外暴露 Jakarta REST 契约。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class NexusJaxRsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(NexusJaxRsConfiguration.class);

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
     * <p>扫描 Bean 定义中<strong>实现类</strong>带 {@link Path} 的单例并注册 Spring 管理实例
     * （含各 {@code Console*Configuration} 中 {@code @Bean} 工厂方法产出的端点）。</p>
     *
     * <p>{@linkplain Ordered#LOWEST_PRECEDENCE} 尽量在其它 {@link ResourceConfigCustomizer} 之后执行，
     * 保证容器里端点 Bean 定义已就绪。</p>
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    ResourceConfigCustomizer nexusJaxRsEndpointCustomizer(ApplicationContext applicationContext) {
        return resourceConfig -> registerPathAnnotatedEndpoints(applicationContext, resourceConfig);
    }

    private static void registerPathAnnotatedEndpoints(
            ApplicationContext applicationContext,
            ResourceConfig resourceConfig) {
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        List<String> registeredPaths = new ArrayList<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            if (!beanFactory.isSingleton(beanName)) {
                continue;
            }
            Class<?> resourceClass = resolveEndpointClass(beanFactory, beanName);
            if (resourceClass == null) {
                continue;
            }
            if (!resourceClass.isAnnotationPresent(Path.class)) {
                continue;
            }
            resourceConfig.register(applicationContext.getBean(beanName));
            Path path = resourceClass.getAnnotation(Path.class);
            String pathValue = path == null ? "" : path.value();
            registeredPaths.add(beanName + " -> " + resourceClass.getName() + " @Path(\"" + pathValue + "\")");
        }
        LOG.info("Jersey registered {} @Path resource bean(s)", registeredPaths.size());
        for (String entry : registeredPaths) {
            LOG.info("Jersey @Path resource: {}", entry);
        }
        if (registeredPaths.isEmpty()) {
            LOG.warn(
                    "Jersey registered zero @Path resources; check @EnableNexusConsole / @EnableNexusPlatform "
                            + "and Console*Configuration endpoint @Bean definitions");
        }
    }

    /**
     * 解析端点实现类。{@code getType(name, false)} 对 {@code @Configuration} 内 {@code @Bean}（尤其 {@code @Lazy}）
     * 常返回 {@code null}，需从工厂方法返回类型读取。
     */
    private static Class<?> resolveEndpointClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (!beanFactory.containsBeanDefinition(beanName)) {
            return null;
        }
        BeanDefinition definition = beanFactory.getBeanDefinition(beanName);
        if (definition instanceof AnnotatedBeanDefinition annotated) {
            MethodMetadata factory = annotated.getFactoryMethodMetadata();
            if (factory != null && StringUtils.hasText(factory.getReturnTypeName())) {
                return ClassUtils.resolveClassName(factory.getReturnTypeName(), beanFactory.getBeanClassLoader());
            }
        }
        if (StringUtils.hasText(definition.getBeanClassName())) {
            Class<?> direct = ClassUtils.resolveClassName(definition.getBeanClassName(), beanFactory.getBeanClassLoader());
            if (direct.isAnnotationPresent(Path.class)) {
                return direct;
            }
        }
        Class<?> type = beanFactory.getType(beanName);
        if (type == null) {
            return null;
        }
        return AopUtils.getTargetClass(type);
    }
}
