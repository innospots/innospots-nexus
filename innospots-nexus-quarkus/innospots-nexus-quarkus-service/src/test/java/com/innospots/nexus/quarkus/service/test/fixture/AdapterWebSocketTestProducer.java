package com.innospots.nexus.quarkus.service.test.fixture;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.service.adapter.test.fixture.AdapterSampleWebSocketHandler;
import com.innospots.nexus.service.adapter.test.fixture.AdapterWebSocketFixtures;
import com.innospots.nexus.service.websocket.handler.WebSocketHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;

/**
 * adapter WebSocket 测试 bean。
 */
@ApplicationScoped
public final class AdapterWebSocketTestProducer {

    @Produces
    @Singleton
    JsonWebSocketCodec adapterWebSocketCodec() {
        return AdapterWebSocketFixtures.codec();
    }

    @Produces
    @Singleton
    WebSocketHandler<Object, Object> adapterSampleWebSocketHandler() {
        return new AdapterSampleWebSocketHandler();
    }

}
