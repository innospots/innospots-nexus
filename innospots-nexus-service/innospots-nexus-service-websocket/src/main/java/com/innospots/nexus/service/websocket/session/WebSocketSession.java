package com.innospots.nexus.service.websocket.session;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import com.innospots.nexus.service.websocket.message.WebSocketMessage;

/**
 * 当前 WebSocket 连接会话视图。
 *
 * @param <I> 入站消息载荷类型
 * @param <O> 出站消息载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public interface WebSocketSession<I, O> {

    /**
     * 返回物理连接标识。
     *
     * @return 连接标识
     */
    String connectionId();

    /**
     * 返回业务会话标识。
     *
     * @return 会话标识
     */
    String sessionId();

    /**
     * 返回连接上下文。
     *
     * @return 上下文
     */
    WebSocketContext context();

    /**
     * 返回入站消息发布者。
     *
     * @return 入站流
     */
    Flow.Publisher<WebSocketMessage<I>> inbound();

    /**
     * 发送出站消息。
     *
     * @param message 出站消息
     * @return 写出完成阶段
     */
    CompletionStage<Void> send(WebSocketMessage<O> message);

    /**
     * 关闭连接。
     *
     * @param close 关闭语义
     * @return 关闭完成阶段
     */
    CompletionStage<Void> close(WebSocketClose close);

    /**
     * 返回连接是否仍处于打开状态。
     *
     * @return 打开时为 {@code true}
     */
    boolean isOpen();
}
