package com.innospots.nexus.spring.service.websocket.bridge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.TextMessage;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.registry.WebSocketRegistry;
import com.innospots.nexus.service.websocket.session.ConnectionState;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

/**
 * 将 Spring Servlet WebSocket 会话适配为标准接口。
 */
public final class SpringWebSocketSessionAdapter<I, O>
        implements WebSocketSession<I, O>, WebSocketRegistry.WebSocketConnectionBinding {

    private final org.springframework.web.socket.WebSocketSession nativeSession;
    private final WebSocketContext context;
    private final JsonWebSocketCodec codec;
    private final Set<String> allowedOutputTypes;
    private volatile ConnectionState state = ConnectionState.OPEN;

    /**
     * 创建适配器。
     *
     * @param nativeSession        原生会话
     * @param serviceContext       服务上下文
     * @param codec                编解码器
     * @param allowedOutputTypes   允许的出站类型
     */
    public SpringWebSocketSessionAdapter(
            org.springframework.web.socket.WebSocketSession nativeSession,
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
    public Flow.Publisher<WebSocketMessage<I>> inbound() {
        SubmissionPublisher<WebSocketMessage<I>> publisher = new SubmissionPublisher<>();
        publisher.close();
        return publisher;
    }

    @Override
    public CompletionStage<Void> send(WebSocketMessage<O> message) {
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
        CompletableFuture<Void> completed = new CompletableFuture<>();
        try {
            nativeSession.close(new org.springframework.web.socket.CloseStatus(close.code(), close.reason()));
            state = ConnectionState.CLOSED;
            completed.complete(null);
        } catch (IOException ex) {
            completed.completeExceptionally(ex);
        }
        return completed;
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
    public CompletionStage<Void> deliver(WebSocketMessage<?> message) {
        Checks.notNull(message, "message");
        ByteBuffer encoded = codec.encode(message);
        byte[] bytes = new byte[encoded.remaining()];
        encoded.get(bytes);
        try {
            nativeSession.sendMessage(new TextMessage(new String(bytes, StandardCharsets.UTF_8)));
            return CompletableFuture.completedFuture(null);
        } catch (IOException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * 复制握手请求头。
     *
     * @param headers 原生请求头
     * @return 小写 map
     */
    public static Map<String, List<String>> handshakeHeaders(HttpHeaders headers) {
        Map<String, List<String>> copied = new LinkedHashMap<>();
        if (headers == null) {
            return copied;
        }
        headers.forEach((name, values) -> copied.put(name.toLowerCase(Locale.ROOT), List.copyOf(values)));
        return copied;
    }
}
