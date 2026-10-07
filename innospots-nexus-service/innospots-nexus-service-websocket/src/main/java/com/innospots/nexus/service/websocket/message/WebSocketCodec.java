package com.innospots.nexus.service.websocket.message;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * WebSocket 帧与标准消息 envelope 之间的编解码。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface WebSocketCodec {

    /**
     * 将帧解码为标准消息。
     *
     * @param frame       完整帧
     * @param payloadType 备用载荷类型
     * @return 解码后的消息
     */
    WebSocketMessage<?> decode(ByteBuffer frame, Type payloadType);

    /**
     * 将标准消息编码为帧。
     *
     * @param message 消息
     * @return 编码帧
     */
    ByteBuffer encode(WebSocketMessage<?> message);
}
