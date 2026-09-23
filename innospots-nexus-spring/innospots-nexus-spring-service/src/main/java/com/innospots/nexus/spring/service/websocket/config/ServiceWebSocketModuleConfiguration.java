package com.innospots.nexus.spring.service.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.core.ServiceCoreConfiguration;

/**
 * WebSocket 中立运行时 Bean（Registry、配置）。路径映射与 Bridge 仍由应用注册。
 *
 * @see EnableNexusServiceWebSocket
 */
@Configuration
@ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
        ServiceCoreConfiguration.class,
        ServiceWebSocketConfiguration.class
})
public class ServiceWebSocketModuleConfiguration {
}
