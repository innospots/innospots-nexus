package com.innospots.nexus.service.websocket.handler;

import java.util.concurrent.Flow;

import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketContext;

/**
 * WebSocket 响应式业务处理器。
 *
 * @param <I> 入站载荷类型
 * @param <O> 出站载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public interface ReactiveWebSocketHandler<I, O> {

    /**
     * 处理已授权入站流并返回出站流。
     *
     * @param context 连接上下文
     * @param inbound 入站消息流
     * @return 出站消息流
     */
    Flow.Publisher<WebSocketMessage<O>> handle(
            WebSocketContext context, Flow.Publisher<WebSocketMessage<I>> inbound);
}
