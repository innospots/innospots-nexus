package com.innospots.nexus.service.websocket.governance;

import com.innospots.nexus.base.util.Checks;

/**
 * WebSocket 入站消息在 {@link com.innospots.nexus.service.contract.invocation.InvocationContext}
 * 中使用的稳定 {@code operationId} 约定。
 *
 * <p>格式 {@code ws.message.{type}}，与 HTTP 操作 id 区分，并作为限流维度
 * {@code operation:...} 与超时表回退键。</p>
 */
public final class WebSocketOperationIds {

    /** 操作 id 前缀。 */
    public static final String MESSAGE_PREFIX = "ws.message.";

    private WebSocketOperationIds() {
    }

    /**
     * 为已注册消息类型生成操作标识。
     *
     * @param messageType codec 中注册的 {@code type} 字段
     * @return 稳定 operationId
     */
    public static String forMessageType(String messageType) {
        Checks.notBlank(messageType, "messageType");
        return MESSAGE_PREFIX + messageType;
    }
}
