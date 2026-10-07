package com.innospots.nexus.service.websocket.registry;

import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketClose;

/**
 * 业务侧 WebSocket 主动发送门面。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface WebSocketService {

    /**
     * 向指定连接发送消息。
     *
     * @param connectionId 连接标识
     * @param message      消息
     * @return 完成阶段
     */
    CompletionStage<Void> send(String connectionId, WebSocketMessage<?> message);

    /**
     * 向业务会话广播消息。
     *
     * @param sessionId 会话标识
     * @param message   消息
     * @return 广播结果
     */
    CompletionStage<BroadcastResult> sendToSession(String sessionId, WebSocketMessage<?> message);

    /**
     * 关闭指定连接。
     *
     * @param connectionId 连接标识
     * @param close        关闭语义
     * @return 完成阶段
     */
    CompletionStage<Void> close(String connectionId, WebSocketClose close);
}
