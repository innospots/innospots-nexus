package com.innospots.nexus.service.websocket.registry;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.ConnectionState;
import com.innospots.nexus.service.websocket.session.WebSocketClose;

/**
 * WebSocket 连接注册表，供 adapter/runtime 使用。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface WebSocketRegistry extends WebSocketService {

    /**
     * 注册连接。
     *
     * @param binding 连接绑定
     */
    void register(WebSocketConnectionBinding binding);

    /**
     * 注销连接。
     *
     * @param connectionId 连接标识
     */
    void unregister(String connectionId);

    /**
     * 按主体与作用域列出连接快照。
     *
     * @param principal 主体
     * @param scope     作用域
     * @return 快照列表
     */
    List<ConnectionSnapshot> listByPrincipal(ServicePrincipal principal, ServiceScope scope);

    /**
     * 已注册连接的出站绑定。
     */
    interface WebSocketConnectionBinding {

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
         * 返回认证域。
         *
         * @return 认证域
         */
        String realm();

        /**
         * 返回连接主体。
         *
         * @return 主体
         */
        ServicePrincipal principal();

        /**
         * 返回资源作用域。
         *
         * @return 作用域
         */
        ServiceScope scope();

        /**
         * 返回连接状态。
         *
         * @return 状态
         */
        ConnectionState state();

        /**
         * 返回允许的出站消息类型。
         *
         * @return 类型集合
         */
        Set<String> allowedOutputTypes();

        /**
         * 投递出站消息。
         *
         * @param message 消息
         * @return 完成阶段
         */
        CompletionStage<Void> deliver(WebSocketMessage<?> message);

        /**
         * 关闭连接。
         *
         * @param close 关闭语义
         * @return 完成阶段
         */
        CompletionStage<Void> close(WebSocketClose close);
    }
}
