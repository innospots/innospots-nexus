package com.innospots.nexus.service.websocket.governance;

import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig;
import com.innospots.nexus.service.websocket.handler.WebSocketHandler;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * 入站消息在「直接业务回调」与「治理引擎派发」之间的统一入口，供 Spring / Quarkus 桥接器调用。
 *
 * <p>{@link WebSocketRuntimeConfig#inboundGovernanceEnabled()} 为 {@code false} 时仅关闭
 * 限流/舱壁/熔断/超时；若 {@link MessageDescriptor#permissionKeys()} 非空，仍经
 * {@code InvocationEngine} 走授权拦截器。</p>
 */
public final class WebSocketInboundMessageHandler {

    private final WebSocketGovernedMessageDispatcher dispatcher;
    private final WebSocketRuntimeConfig config;

    /**
     * 创建入站处理器。
     *
     * @param dispatcher 治理派发器
     * @param config     WebSocket 运行时配置
     */
    public WebSocketInboundMessageHandler(
            WebSocketGovernedMessageDispatcher dispatcher,
            WebSocketRuntimeConfig config) {
        this.dispatcher = Checks.notNull(dispatcher, "dispatcher");
        this.config = Checks.notNull(config, "config");
    }

    /**
     * 处理已解码的入站消息。
     *
     * @param handler    业务处理器
     * @param session    连接会话
     * @param message    解码后的消息
     * @param descriptor 与 {@code message.type()} 对应的描述符
     * @param <I>        入站载荷类型
     * @param <O>        出站载荷类型
     * @return 处理完成阶段
     */
    public <I, O> CompletionStage<Void> handle(
            WebSocketHandler<I, O> handler,
            WebSocketSession<I, O> session,
            WebSocketMessage<I> message,
            MessageDescriptor descriptor) {
        Checks.notNull(handler, "handler");
        Checks.notNull(session, "session");
        Checks.notNull(message, "message");
        Checks.notNull(descriptor, "descriptor");
        if (!shouldUseInvocationEngine(descriptor)) {
            return handler.onMessage(session, message);
        }
        boolean applyGovernanceKeys = config.inboundGovernanceEnabled();
        return dispatcher.dispatch(session, descriptor, applyGovernanceKeys, () -> handler.onMessage(session, message));
    }

    private boolean shouldUseInvocationEngine(MessageDescriptor descriptor) {
        if (!WebSocketMessagePolicies.usesInvocationEngine(descriptor)) {
            return false;
        }
        if (config.inboundGovernanceEnabled()) {
            return true;
        }
        return WebSocketMessagePolicies.requiresEngineWhenGovernanceDisabled(descriptor);
    }
}
