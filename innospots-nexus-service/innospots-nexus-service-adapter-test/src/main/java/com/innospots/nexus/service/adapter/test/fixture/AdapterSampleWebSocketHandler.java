package com.innospots.nexus.service.adapter.test.fixture;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.websocket.handler.AbstractWebSocketHandler;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * adapter 场景共享 WebSocket 业务处理器。
 */
public final class AdapterSampleWebSocketHandler extends AbstractWebSocketHandler<Object, Object> {

    @Override
    public CompletionStage<Void> onOpen(WebSocketSession<Object, Object> session) {
        WebSocketMessage<Object> ready = new WebSocketMessage<>(
                "0",
                AdapterWebSocketFixtures.READY_TYPE,
                null,
                0L,
                Instant.now(),
                "ready",
                Map.of());
        return session.send(ready);
    }

    @Override
    public CompletionStage<Void> onMessage(WebSocketSession<Object, Object> session, WebSocketMessage<Object> message) {
        return session.send(message);
    }

    @Override
    public CompletionStage<Void> onError(
            com.innospots.nexus.service.websocket.session.WebSocketContext context,
            com.innospots.nexus.base.exception.NexusException failure) {
        return CompletableFuture.completedFuture(null);
    }
}
