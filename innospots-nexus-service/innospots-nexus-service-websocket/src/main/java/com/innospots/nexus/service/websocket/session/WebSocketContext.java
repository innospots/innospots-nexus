package com.innospots.nexus.service.websocket.session;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * WebSocket 连接上下文。{@code connectionId} 标识物理连接，{@code sessionId} 标识业务会话。
 *
 * @param connectionId 物理连接标识
 * @param sessionId    业务会话标识
 * @param service      调用服务上下文
 * @author Smars
 * @date 2026/09/15
 */
public record WebSocketContext(String connectionId, String sessionId, ServiceContext service) {

    public WebSocketContext {
        Checks.notBlank(connectionId, "connectionId");
        Checks.notBlank(sessionId, "sessionId");
        Checks.notNull(service, "service");
    }
}
