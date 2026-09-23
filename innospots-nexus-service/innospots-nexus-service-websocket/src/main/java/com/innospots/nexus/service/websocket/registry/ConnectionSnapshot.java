package com.innospots.nexus.service.websocket.registry;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.websocket.session.ConnectionState;

/**
 * 已注册连接快照。
 *
 * @param connectionId 物理连接标识
 * @param sessionId    业务会话标识
 * @param realm        认证域
 * @param scope        资源作用域
 * @param principal    连接主体
 * @param state        连接状态
 * @author Smars
 * @date 2026/09/15
 */
public record ConnectionSnapshot(
        String connectionId,
        String sessionId,
        String realm,
        ServiceScope scope,
        ServicePrincipal principal,
        ConnectionState state
) {

    public ConnectionSnapshot {
        Checks.notBlank(connectionId, "connectionId");
        Checks.notBlank(sessionId, "sessionId");
        Checks.notBlank(realm, "realm");
        Checks.notNull(scope, "scope");
        Checks.notNull(principal, "principal");
        Checks.notNull(state, "state");
    }
}
