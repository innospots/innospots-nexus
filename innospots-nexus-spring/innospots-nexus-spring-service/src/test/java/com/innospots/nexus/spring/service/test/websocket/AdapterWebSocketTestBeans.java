package com.innospots.nexus.spring.service.test.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.innospots.nexus.service.adapter.test.fixture.AdapterSampleWebSocketHandler;
import com.innospots.nexus.service.adapter.test.fixture.AdapterWebSocketFixtures;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.registry.LocalWebSocketRegistry;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;
import com.innospots.nexus.spring.service.websocket.bridge.SpringWebSocketEndpointBridge;

/**
 * adapter WebSocket 测试 Bean。
 */
@Configuration
@Profile("!webflux")
public class AdapterWebSocketTestBeans {

    @Bean
    AdapterSampleWebSocketHandler adapterSampleWebSocketHandler() {
        return new AdapterSampleWebSocketHandler();
    }

    @Bean
    JsonWebSocketCodec adapterWebSocketCodec() {
        return AdapterWebSocketFixtures.codec();
    }

    @Bean
    SpringWebSocketEndpointBridge adapterWebSocketEndpointBridge(
            AdapterSampleWebSocketHandler adapterSampleWebSocketHandler,
            JsonWebSocketCodec adapterWebSocketCodec,
            LocalWebSocketRegistry serviceWebSocketRegistry,
            ServiceTransportSupport serviceTransportSupport,
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            WebSocketInboundMessageHandler serviceWebSocketInboundMessageHandler) {
        return new SpringWebSocketEndpointBridge(
                adapterSampleWebSocketHandler,
                adapterWebSocketCodec,
                serviceWebSocketRegistry,
                serviceTransportSupport,
                serviceThreadBoundServiceContext,
                serviceWebSocketInboundMessageHandler);
    }
}
