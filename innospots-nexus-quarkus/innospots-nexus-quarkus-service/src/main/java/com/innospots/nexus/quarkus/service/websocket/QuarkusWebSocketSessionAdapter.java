package com.innospots.nexus.quarkus.service.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

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

import io.quarkus.websockets.next.CloseReason;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * 将 Quarkus WebSockets Next 连接适配为标准接口。
 */
public final class QuarkusWebSocketSessionAdapter<I, O>
        implements WebSocketSession<I, O>, WebSocketRegistry.WebSocketConnectionBinding {

    private final WebSocketConnection connection;
    private final WebSocketContext context;
    private final JsonWebSocketCodec codec;
    private final Set<String> allowedOutputTypes;
    private volatile ConnectionState state = ConnectionState.OPEN;

    /**
     * 创建适配器。
     *
     * @param connection           原生连接
     * @param serviceContext       服务上下文
     * @param codec                编解码器
     * @param allowedOutputTypes   允许的出站类型
     */
    public QuarkusWebSocketSessionAdapter(
            WebSocketConnection connection,
            ServiceContext serviceContext,
            JsonWebSocketCodec codec,
            Set<String> allowedOutputTypes) {
        this.connection = Checks.notNull(connection, "connection");
        this.codec = Checks.notNull(codec, "codec");
        this.allowedOutputTypes = Set.copyOf(Checks.notNull(allowedOutputTypes, "allowedOutputTypes"));
        String connectionId = connection.id();
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
        return connection.isOpen();
    }

    @Override
    public CompletionStage<Void> close(WebSocketClose close) {
        Checks.notNull(close, "close");
        state = ConnectionState.CLOSING;
        CompletableFuture<Void> completed = new CompletableFuture<>();
        connection.close(new CloseReason(close.code(), close.reason()))
                .subscribe()
                .with(
                        ignored -> {
                            state = ConnectionState.CLOSED;
                            completed.complete(null);
                        },
                        completed::completeExceptionally);
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
        CompletableFuture<Void> completed = new CompletableFuture<>();
        connection.sendText(new String(bytes, StandardCharsets.UTF_8))
                .subscribe()
                .with(ignored -> completed.complete(null), completed::completeExceptionally);
        return completed;
    }

    /**
     * 复制握手请求头。
     *
     * @param connection 原生连接
     * @return 小写 map
     */
    public static Map<String, java.util.List<String>> handshakeHeaders(WebSocketConnection connection) {
        Map<String, java.util.List<String>> headers = new java.util.LinkedHashMap<>();
        connection.handshakeRequest().headers().forEach((name, values) -> {
            headers.put(name.toLowerCase(Locale.ROOT), java.util.List.copyOf(values));
        });
        return headers;
    }
}
