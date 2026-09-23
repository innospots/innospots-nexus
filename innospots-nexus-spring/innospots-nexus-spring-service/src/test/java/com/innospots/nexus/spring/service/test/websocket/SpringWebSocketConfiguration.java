package com.innospots.nexus.spring.service.test.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.spring.service.websocket.bridge.SpringWebSocketEndpointBridge;

/**
 * 注册 adapter WebSocket 端点。
 */
@Configuration
@EnableWebSocket
@Profile("!webflux")
public class SpringWebSocketConfiguration implements WebSocketConfigurer {

    private final SpringWebSocketEndpointBridge adapterWebSocketEndpointBridge;

    public SpringWebSocketConfiguration(SpringWebSocketEndpointBridge adapterWebSocketEndpointBridge) {
        this.adapterWebSocketEndpointBridge = adapterWebSocketEndpointBridge;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(adapterWebSocketEndpointBridge, AdapterScenarioPaths.WEBSOCKET).setAllowedOrigins("*");
    }
}
