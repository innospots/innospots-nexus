package com.innospots.nexus.spring.service.test.webflux;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.innospots.nexus.service.adapter.test.fixture.AdapterSampleWebSocketHandler;
import com.innospots.nexus.service.adapter.test.fixture.AdapterWebSocketFixtures;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.registry.LocalWebSocketRegistry;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;
import com.innospots.nexus.spring.service.websocket.bridge.SpringReactiveWebSocketEndpointBridge;

/**
 * 注册 adapter WebFlux WebSocket 端点。
 */
@Configuration
@Profile("webflux")
public class ReactiveWebSocketConfiguration {

    @Bean
    AdapterSampleWebSocketHandler adapterSampleWebSocketHandler() {
        return new AdapterSampleWebSocketHandler();
    }

    @Bean
    JsonWebSocketCodec adapterWebSocketCodec() {
        return AdapterWebSocketFixtures.codec();
    }

    @Bean
    SpringReactiveWebSocketEndpointBridge adapterReactiveWebSocketEndpointBridge(
            AdapterSampleWebSocketHandler adapterSampleWebSocketHandler,
            JsonWebSocketCodec adapterWebSocketCodec,
            LocalWebSocketRegistry serviceWebSocketRegistry,
            ServiceTransportSupport serviceTransportSupport,
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            WebSocketInboundMessageHandler serviceWebSocketInboundMessageHandler) {
        return new SpringReactiveWebSocketEndpointBridge(
                adapterSampleWebSocketHandler,
                adapterWebSocketCodec,
                serviceWebSocketRegistry,
                serviceTransportSupport,
                serviceThreadBoundServiceContext,
                serviceWebSocketInboundMessageHandler);
    }

    @Bean
    HandlerMapping adapterWebSocketHandlerMapping(
            SpringReactiveWebSocketEndpointBridge adapterReactiveWebSocketEndpointBridge) {
        Map<String, org.springframework.web.reactive.socket.WebSocketHandler> urlMap =
                Map.of(AdapterScenarioPaths.WEBSOCKET, adapterReactiveWebSocketEndpointBridge);
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(urlMap);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    WebSocketHandlerAdapter adapterWebSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
