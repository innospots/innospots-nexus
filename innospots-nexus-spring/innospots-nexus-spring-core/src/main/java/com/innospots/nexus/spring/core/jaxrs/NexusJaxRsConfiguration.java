package com.innospots.nexus.spring.core.jaxrs;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.ContextResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innospots.nexus.base.json.Jsons;

/**
 * Jakarta REST 的 Spring 装配入口：提供 {@link ResourceConfig} Bean 与 JSON {@link ContextResolver}。
 *
 * <p>Spring Boot {@code spring-boot-starter-jersey} 只负责 Servlet/Filter 与自动配置，不会自动注册业务
 * {@link Path} 端点；{@link NexusJaxRsResourceConfig} 在创建时把容器中的根资源注册进去。</p>
 *
 * <p>默认 Servlet 模式由 Jersey 直接处理请求，无需 Spring MVC。Filter 模式下，
 * {@link NexusJaxRsProperties#isForwardOn404()} 控制未匹配请求是否进入后续 Servlet 链；
 * 宿主须提供匹配的 Servlet，或启用 {@code server.servlet.register-default-servlet}。</p>
 *
 * @see NexusJaxRsResourceConfig
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
            ApplicationContext applicationContext,
            List<NexusJerseyResourceConfigurer> jerseyResourceConfigurers,
            ContextResolver<ObjectMapper> jsonsObjectMapperResolver,
            NexusJaxRsProperties jaxRsProperties) {
        NexusJaxRsResourceConfig resourceConfig = new NexusJaxRsResourceConfig(
                jerseyResourceConfigurers, jsonsObjectMapperResolver, jaxRsProperties);
        registerPathAnnotatedEndpoints(applicationContext, resourceConfig);
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
     * 注册 Spring 容器中的 JAX-RS 根资源（按 BeanDefinition 解析类型，适配 {@code @Configuration} 内 {@code @Bean} 工厂端点）。
     *
     * <p>使用 HK2 绑定 Spring 单例 + {@code register(Class)}，避免 {@code register(Object)} 触发
     * Jersey provider 误报（Jersey #3700）。仅 {@code register(Class)} 且无绑定时，本宿主下曾出现 404。</p>
     */
    public static void registerPathAnnotatedEndpoints(
            ApplicationContext applicationContext,
            ResourceConfig resourceConfig) {
        ConfigurableListableBeanFactory beanFactory =
                (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        List<String> registeredPaths = new ArrayList<>();
        List<SpringManagedJerseyResourceBinding> bindings = new ArrayList<>();
        Set<Class<?>> registeredClasses = new HashSet<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            if (!beanFactory.isSingleton(beanName)) {
                continue;
            }
            Class<?> resourceClass = resolveEndpointClass(beanFactory, beanName);
            if (resourceClass == null || !resourceClass.isAnnotationPresent(Path.class)) {
                continue;
            }
            if (!registeredClasses.add(resourceClass)) {
                continue;
            }
            Object resourceInstance = applicationContext.getBean(beanName);
            bindings.add(new SpringManagedJerseyResourceBinding(resourceInstance, resourceClass));
            appendRegisteredPath(registeredPaths, beanName, resourceClass);
        }
        if (!bindings.isEmpty()) {
            resourceConfig.register(new SpringManagedJerseyResourceBinder(bindings));
            for (SpringManagedJerseyResourceBinding binding : bindings) {
                resourceConfig.register(binding.resourceClass());
            }
        }

        LOG.info("Jersey registered {} @Path resource bean(s)", registeredPaths.size());
        if (LOG.isDebugEnabled()) {
            for (String entry : registeredPaths) {
                LOG.debug("Jersey @Path resource: {}", entry);
            }
        }
        if (registeredPaths.isEmpty()) {
            LOG.warn("Jersey registered zero @Path resources; check host @Enable* bootstrap annotations "
                    + "and endpoint @Bean definitions");
        }
    }

    private static void appendRegisteredPath(List<String> registeredPaths, String beanName, Class<?> resourceClass) {
        Path path = resourceClass.getAnnotation(Path.class);
        String pathValue = path == null ? "" : path.value();
        registeredPaths.add(beanName + " -> " + resourceClass.getName() + " @Path(\"" + pathValue + "\")");
    }

    /**
     * 解析端点实现类。{@code getType(name, false)} 对 {@code @Configuration} 内 {@code @Bean}（尤其 {@code @Lazy}）
     * 常返回 {@code null}，需从工厂方法返回类型读取。
     */
    public static Class<?> resolveEndpointClass(ConfigurableListableBeanFactory beanFactory, String beanName) {
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
