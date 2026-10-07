package com.innospots.nexus.service.websocket.handler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * WebSocket 回调式业务处理器。
 *
 * @param <I> 入站载荷类型
 * @param <O> 出站载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public interface WebSocketHandler<I, O> {

    /**
     * 连接打开回调。
     *
     * @param session 当前会话
     * @return 完成阶段
     */
    CompletionStage<Void> onOpen(WebSocketSession<I, O> session);

    /**
     * 收到消息回调。
     *
     * @param session 当前会话
     * @param message 入站消息
     * @return 完成阶段
     */
    CompletionStage<Void> onMessage(WebSocketSession<I, O> session, WebSocketMessage<I> message);

    /**
     * 连接关闭回调。
     *
     * @param context 连接上下文
     * @param close   关闭语义
     * @return 完成阶段
     */
    CompletionStage<Void> onClose(WebSocketContext context, WebSocketClose close);

    /**
     * 连接错误回调。
     *
     * @param context 连接上下文
     * @param failure 失败
     * @return 完成阶段
     */
    CompletionStage<Void> onError(WebSocketContext context, NexusException failure);
}
