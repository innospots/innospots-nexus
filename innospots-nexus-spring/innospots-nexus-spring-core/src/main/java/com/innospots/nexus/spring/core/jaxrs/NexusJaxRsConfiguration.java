package com.innospots.nexus.spring.core.jaxrs;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.ContextResolver;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.glassfish.jersey.servlet.ServletProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.json.Jsons;

/**
 * 将 Spring 容器中带 {@link Path} 的 Bean 注册为 Jersey 资源，直接对外暴露 Jakarta REST 契约。
 *
 * <p>Filter 模式下 404 是否透传 Spring MVC 由 {@link NexusJaxRsProperties#isForwardOn404()}
 * （{@code nexus.web.jersey.forward-on-404}）控制（默认透传）。</p>
 *
 * @see NexusJerseyResourceConfigurer
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResourceConfig.class)
@EnableConfigurationProperties(NexusJaxRsProperties.class)
public class NexusJaxRsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(NexusJaxRsConfiguration.class);

    @Bean
    ResourceConfig nexusJaxRsResourceConfig(
            List<NexusJerseyResourceConfigurer> jerseyResourceConfigurers,
            ContextResolver<ObjectMapper> jsonsObjectMapperResolver,
            NexusJaxRsProperties jaxRsProperties) {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(SpringComponentProvider.class);
        resourceConfig.register(jsonsObjectMapperResolver);
        resourceConfig.property(
                ServletProperties.FILTER_FORWARD_ON_404, jaxRsProperties.isForwardOn404());
        for (NexusJerseyResourceConfigurer configurer : jerseyResourceConfigurers) {
            configurer.configure(resourceConfig);
        }
        return resourceConfig;
    }

    /**
     * 让 Jersey 的 Jackson 提供者（{@code JacksonJaxbJsonProvider}）复用 {@link Jsons#mapper()}，
     * 使 {@code I18nModule} 的 I18nObject 契约、未知字段容忍与 ISO 日期序列化对全部 JAX-RS 端点生效。
     *
     * <p>响应字段脱敏（{@code MaskingModule}）不属于本 resolver，由出参侧按需处理。</p>
     *
     * @return 绑定 {@link Jsons#mapper()} 的 resolver
     */
    @Bean
    ContextResolver<ObjectMapper> jsonsObjectMapperResolver() {
        return type -> Jsons.mapper();
    }

    /**
     * <p>扫描 Bean 定义中<strong>实现类</strong>带 {@link Path} 的单例并注册 Spring 管理实例
     * （含各 {@code *Configuration} 中 {@code @Bean} 工厂方法产出的端点）。</p>
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
            LOG.warn("Jersey registered zero @Path resources; check host @Enable* bootstrap annotations "
                    + "and endpoint @Bean definitions");
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
