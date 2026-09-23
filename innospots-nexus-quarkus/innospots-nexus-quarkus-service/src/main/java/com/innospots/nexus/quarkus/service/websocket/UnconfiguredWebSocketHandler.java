package com.innospots.nexus.quarkus.service.websocket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.websocket.handler.AbstractWebSocketHandler;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * 占位 {@link com.innospots.nexus.service.websocket.handler.WebSocketHandler}；
 * 宿主应通过 CDI {@code @Produces} 提供真实实现以覆盖 {@link io.quarkus.arc.DefaultBean}。
 */
public final class UnconfiguredWebSocketHandler extends AbstractWebSocketHandler<Object, Object> {

    @Override
    public CompletionStage<Void> onMessage(WebSocketSession<Object, Object> session, WebSocketMessage<Object> message) {
        return CompletableFuture.completedFuture(null);
    }
}
