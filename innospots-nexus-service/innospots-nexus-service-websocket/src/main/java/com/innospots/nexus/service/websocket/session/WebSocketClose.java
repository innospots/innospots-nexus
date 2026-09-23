package com.innospots.nexus.service.websocket.session;

import com.innospots.nexus.base.util.Checks;

/**
 * WebSocket 关闭帧语义。
 *
 * @param code   关闭码
 * @param reason 关闭原因
 * @author Smars
 * @date 2026/09/15
 */
public record WebSocketClose(int code, String reason) {

    public WebSocketClose {
        Checks.isTrue(code >= 1000 && code <= 4999, "code must be a valid WebSocket close code");
        reason = reason == null ? "" : reason;
    }
}
