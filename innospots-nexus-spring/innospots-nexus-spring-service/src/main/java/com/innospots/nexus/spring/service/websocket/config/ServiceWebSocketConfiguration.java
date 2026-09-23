package com.innospots.nexus.spring.service.websocket.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig;
import com.innospots.nexus.service.websocket.governance.WebSocketGovernedMessageDispatcher;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.registry.LocalWebSocketRegistry;
import com.innospots.nexus.service.websocket.registry.WebSocketRegistry;
import com.innospots.nexus.service.websocket.registry.WebSocketService;

/**
 * WebSocket 运行时 Bean。
 */
@Configuration
@ConditionalOnClass(name = "com.innospots.nexus.service.websocket.registry.WebSocketRegistry")
public class ServiceWebSocketConfiguration {

    @Bean
    @ConditionalOnMissingBean
    WebSocketRuntimeConfig serviceWebSocketRuntimeConfig() {
        return WebSocketRuntimeConfig.defaults();
    }

    @Bean
    @ConditionalOnMissingBean
    LocalWebSocketRegistry serviceWebSocketRegistry(
            WebSocketRuntimeConfig serviceWebSocketRuntimeConfig,
            ThreadBoundServiceContext serviceThreadBoundServiceContext) {
        return LocalWebSocketRegistry.builder()
                .config(serviceWebSocketRuntimeConfig)
                .contexts(serviceThreadBoundServiceContext)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketRegistry.class)
    WebSocketRegistry serviceWebSocketRegistryFacade(LocalWebSocketRegistry serviceWebSocketRegistry) {
        return serviceWebSocketRegistry;
    }

    @Bean
    @ConditionalOnMissingBean(WebSocketService.class)
    WebSocketService serviceWebSocketService(LocalWebSocketRegistry serviceWebSocketRegistry) {
        return serviceWebSocketRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    WebSocketGovernedMessageDispatcher serviceWebSocketGovernedMessageDispatcher(
            InvocationEngine serviceInvocationEngine) {
        return new WebSocketGovernedMessageDispatcher(serviceInvocationEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    WebSocketInboundMessageHandler serviceWebSocketInboundMessageHandler(
            WebSocketGovernedMessageDispatcher serviceWebSocketGovernedMessageDispatcher,
            WebSocketRuntimeConfig serviceWebSocketRuntimeConfig) {
        return new WebSocketInboundMessageHandler(
                serviceWebSocketGovernedMessageDispatcher,
                serviceWebSocketRuntimeConfig);
    }
}
