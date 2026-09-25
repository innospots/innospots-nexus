package com.innospots.nexus.spring.console.jaxrs;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * 在 {@link NexusJaxRsConfiguration} 创建的 {@link ResourceConfig} 上追加 Jersey 路由。
 */
public interface NexusJerseyResourceConfigurer {

    void configure(ResourceConfig resourceConfig);
}
