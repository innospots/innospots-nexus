package com.innospots.nexus.spring.service.websocket.bridge;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import org.springframework.web.reactive.socket.WebSocketMessage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.registry.WebSocketRegistry;
import com.innospots.nexus.service.websocket.session.ConnectionState;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

import reactor.core.publisher.Mono;

/**
 * 将 Spring WebFlux WebSocket 会话适配为标准接口。
 */
public final class SpringReactiveWebSocketSessionAdapter<I, O>
        implements WebSocketSession<I, O>, WebSocketRegistry.WebSocketConnectionBinding {

    private final org.springframework.web.reactive.socket.WebSocketSession nativeSession;
    private final WebSocketContext context;
    private final JsonWebSocketCodec codec;
    private final Set<String> allowedOutputTypes;
    private volatile ConnectionState state = ConnectionState.OPEN;

    /**
     * 创建适配器。
     *
     * @param nativeSession      原生会话
     * @param serviceContext     服务上下文
     * @param codec              编解码器
     * @param allowedOutputTypes 允许的出站类型
     */
    public SpringReactiveWebSocketSessionAdapter(
            org.springframework.web.reactive.socket.WebSocketSession nativeSession,
            ServiceContext serviceContext,
            JsonWebSocketCodec codec,
            Set<String> allowedOutputTypes) {
        this.nativeSession = Checks.notNull(nativeSession, "nativeSession");
        this.codec = Checks.notNull(codec, "codec");
        this.allowedOutputTypes = Set.copyOf(Checks.notNull(allowedOutputTypes, "allowedOutputTypes"));
        String connectionId = nativeSession.getId();
        this.context = new WebSocketContext(connectionId, connectionId, serviceContext);
    }

    @Override
    public String connectionId() {
        return context.connectionId();
    }

    @Override
    public String sessionId() {
        return context.sessionId();
    }

    @Override
    public WebSocketContext context() {
        return context;
    }

    @Override
    public Flow.Publisher<com.innospots.nexus.service.websocket.message.WebSocketMessage<I>> inbound() {
        SubmissionPublisher<com.innospots.nexus.service.websocket.message.WebSocketMessage<I>> publisher =
                new SubmissionPublisher<>();
        publisher.close();
        return publisher;
    }

    @Override
    public CompletionStage<Void> send(com.innospots.nexus.service.websocket.message.WebSocketMessage<O> message) {
        return deliver(message);
    }

    @Override
    public boolean isOpen() {
        return nativeSession.isOpen();
    }

    @Override
    public CompletionStage<Void> close(WebSocketClose close) {
        Checks.notNull(close, "close");
        state = ConnectionState.CLOSING;
        return nativeSession.close(new org.springframework.web.reactive.socket.CloseStatus(close.code(), close.reason()))
                .doOnSuccess(ignored -> state = ConnectionState.CLOSED)
                .toFuture();
    }

    @Override
    public String realm() {
        return context.service().security().realm();
    }

    @Override
    public ServicePrincipal principal() {
        return context.service().security();
    }

    @Override
    public ServiceScope scope() {
        return context.service().scope();
    }

    @Override
    public ConnectionState state() {
        return state;
    }

    @Override
    public Set<String> allowedOutputTypes() {
        return allowedOutputTypes;
    }

    @Override
    public CompletionStage<Void> deliver(com.innospots.nexus.service.websocket.message.WebSocketMessage<?> message) {
        Checks.notNull(message, "message");
        ByteBuffer encoded = codec.encode(message);
        byte[] bytes = new byte[encoded.remaining()];
        encoded.get(bytes);
        WebSocketMessage frame = nativeSession.textMessage(new String(bytes, StandardCharsets.UTF_8));
        return nativeSession.send(Mono.just(frame)).toFuture();
    }
}
