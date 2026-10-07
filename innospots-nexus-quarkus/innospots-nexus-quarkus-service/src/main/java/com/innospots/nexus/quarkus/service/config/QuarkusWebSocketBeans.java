package com.innospots.nexus.quarkus.service.config;

import com.innospots.nexus.quarkus.service.rest.ServiceTransportSupport;
import com.innospots.nexus.quarkus.service.websocket.QuarkusWebSocketEndpointBridge;
import com.innospots.nexus.quarkus.service.websocket.UnconfiguredWebSocketHandler;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig;
import com.innospots.nexus.service.websocket.governance.WebSocketGovernedMessageDispatcher;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.handler.WebSocketHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.registry.LocalWebSocketRegistry;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * WebSocket 默认 CDI bean。
 */
@ApplicationScoped
public final class QuarkusWebSocketBeans {

    @Inject
    ServiceRuntimeHolder serviceRuntimeHolder;

    @Produces
    @Singleton
    @DefaultBean
    WebSocketRuntimeConfig webSocketRuntimeConfig() {
        return WebSocketRuntimeConfig.defaults();
    }

    @Produces
    @Singleton
    @DefaultBean
    LocalWebSocketRegistry localWebSocketRegistry(WebSocketRuntimeConfig webSocketRuntimeConfig) {
        return LocalWebSocketRegistry.builder()
                .config(webSocketRuntimeConfig)
                .contexts(serviceRuntimeHolder.contexts())
                .build();
    }

    @Produces
    @Singleton
    @DefaultBean
    JsonWebSocketCodec defaultJsonWebSocketCodec() {
        return JsonWebSocketCodec.builder().build();
    }

    @Produces
    @Singleton
    @DefaultBean
    WebSocketHandler<Object, Object> defaultWebSocketHandler() {
        return new UnconfiguredWebSocketHandler();
    }

    @Produces
    @Singleton
    @DefaultBean
    WebSocketGovernedMessageDispatcher webSocketGovernedMessageDispatcher() {
        InvocationEngine engine = serviceRuntimeHolder.invocationEngine();
        return new WebSocketGovernedMessageDispatcher(engine);
    }

    @Produces
    @Singleton
    @DefaultBean
    WebSocketInboundMessageHandler webSocketInboundMessageHandler(
            WebSocketGovernedMessageDispatcher webSocketGovernedMessageDispatcher,
            WebSocketRuntimeConfig webSocketRuntimeConfig) {
        return new WebSocketInboundMessageHandler(webSocketGovernedMessageDispatcher, webSocketRuntimeConfig);
    }

    @Produces
    @Singleton
    @DefaultBean
    QuarkusWebSocketEndpointBridge quarkusWebSocketEndpointBridge(
            WebSocketHandler<Object, Object> handler,
            JsonWebSocketCodec codec,
            LocalWebSocketRegistry localWebSocketRegistry,
            ServiceTransportSupport transportSupport,
            WebSocketInboundMessageHandler webSocketInboundMessageHandler) {
        return new QuarkusWebSocketEndpointBridge(
                handler,
                codec,
                localWebSocketRegistry,
                transportSupport,
                serviceRuntimeHolder,
                webSocketInboundMessageHandler);
    }
}
