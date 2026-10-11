package com.innospots.nexus.spring.core.jaxrs;

import jakarta.ws.rs.ext.ContextResolver;

import java.util.List;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 宿主 Jersey {@link ResourceConfig}：由 {@link NexusJaxRsConfiguration} 注册为 Bean，
 * 供 {@link org.springframework.boot.jersey.autoconfigure.JerseyAutoConfiguration} 装配。
 *
 * <p>端点实例由 {@link NexusJaxRsConfiguration} 在返回本 {@link ResourceConfig} Bean 之前注册。不设
 * {@code @ApplicationPath}，根路径由
 * {@code spring.jersey.application-path} 与各资源 {@link jakarta.ws.rs.Path} 决定。</p>
 *
 * @see NexusJerseyResourceConfigurer
 */
public final class NexusJaxRsResourceConfig extends ResourceConfig {

    public NexusJaxRsResourceConfig(
            List<NexusJerseyResourceConfigurer> jerseyResourceConfigurers,
            ContextResolver<ObjectMapper> jsonsObjectMapperResolver,
            NexusJaxRsProperties jaxRsProperties) {
        register(jsonsObjectMapperResolver);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        property(
                ServletProperties.FILTER_FORWARD_ON_404, jaxRsProperties.isForwardOn404());
        for (NexusJerseyResourceConfigurer configurer : jerseyResourceConfigurers) {
            configurer.configure(this);
        }
    }
}
