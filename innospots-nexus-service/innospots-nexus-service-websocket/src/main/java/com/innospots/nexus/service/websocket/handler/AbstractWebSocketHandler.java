package com.innospots.nexus.service.websocket.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * 提供 {@link WebSocketHandler} 默认生命周期回调。
 *
 * @param <I> 入站载荷类型
 * @param <O> 出站载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public abstract class AbstractWebSocketHandler<I, O> implements WebSocketHandler<I, O> {

    @Override
    public CompletionStage<Void> onOpen(WebSocketSession<I, O> session) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> onClose(WebSocketContext context, WebSocketClose close) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> onError(WebSocketContext context, NexusException failure) {
        return CompletableFuture.completedFuture(null);
    }
}
