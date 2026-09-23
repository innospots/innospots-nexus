package com.innospots.nexus.quarkus.service.websocket;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.quarkus.service.config.ServiceRuntimeHolder;
import com.innospots.nexus.quarkus.service.rest.ServiceTransportSupport;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.handler.WebSocketHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.registry.WebSocketRegistry;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;

import io.quarkus.websockets.next.UserData;
import io.quarkus.websockets.next.WebSocketConnection;

import jakarta.enterprise.inject.Vetoed;

/**
 * 将 Quarkus WebSockets Next 回调转发到标准 {@link WebSocketHandler}。
 *
 * <p>由 {@link com.innospots.nexus.quarkus.service.config.QuarkusWebSocketBeans} 显式 {@code @Produces}，
 * 避免 CDI {@code bean-discovery-mode=all} 将本类注册为无法解析依赖的 bean。</p>
 */
@Vetoed
public final class QuarkusWebSocketEndpointBridge {

    private static final UserData.TypedKey<QuarkusWebSocketSessionAdapter<Object, Object>> ADAPTER_KEY =
            new UserData.TypedKey<>("nexus.websocket.adapter");

    private final WebSocketHandler<Object, Object> handler;
    private final JsonWebSocketCodec codec;
    private final WebSocketRegistry registry;
    private final ServiceTransportSupport transportSupport;
    private final ThreadBoundServiceContext contexts;
    private final WebSocketInboundMessageHandler inboundMessageHandler;
    private final Set<String> allowedOutputTypes;

    /**
     * 创建桥接器。
     *
     * @param handler               标准处理器
     * @param codec                 编解码器
     * @param registry              连接注册表
     * @param transportSupport      传输支持
     * @param serviceRuntimeHolder  运行时持有者
     * @param inboundMessageHandler 入站治理派发
     */
    public QuarkusWebSocketEndpointBridge(
            WebSocketHandler<Object, Object> handler,
            JsonWebSocketCodec codec,
            WebSocketRegistry registry,
            ServiceTransportSupport transportSupport,
            ServiceRuntimeHolder serviceRuntimeHolder,
            WebSocketInboundMessageHandler inboundMessageHandler) {
        this.handler = Checks.notNull(handler, "handler");
        this.codec = Checks.notNull(codec, "codec");
        this.registry = Checks.notNull(registry, "registry");
        this.transportSupport = Checks.notNull(transportSupport, "transportSupport");
        this.contexts = Checks.notNull(serviceRuntimeHolder, "serviceRuntimeHolder").contexts();
        this.inboundMessageHandler = Checks.notNull(inboundMessageHandler, "inboundMessageHandler");
        this.allowedOutputTypes = Set.copyOf(codec.registeredTypes());
    }

    /**
     * 处理连接打开。
     *
     * @param connection 原生连接
     */
    public void onOpen(WebSocketConnection connection) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = createAdapter(connection);
        connection.userData().put(ADAPTER_KEY, adapter);
        registry.register(adapter);
        runWithContext(adapter.context().service(), () -> handler.onOpen(adapter));
    }

    /**
     * 处理文本消息。
     *
     * @param payload    文本载荷
     * @param connection 原生连接
     */
    public void onTextMessage(String payload, WebSocketConnection connection) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = requireAdapter(connection);
        runWithContext(adapter.context().service(), () -> {
            try {
                WebSocketMessage<?> decoded = codec.decode(
                        ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8)),
                        String.class);
                MessageDescriptor descriptor = codec.descriptor(decoded.type());
                return inboundMessageHandler.handle(handler, adapter, castMessage(decoded), descriptor);
            } catch (NexusException failure) {
                connection.sendText(errorPayload(failure)).subscribe().with(item -> {
                }, error -> {
                });
                return null;
            }
        });
    }

    /**
     * 处理连接关闭。
     *
     * @param connection 原生连接
     */
    public void onClose(WebSocketConnection connection) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = removeAdapter(connection);
        if (adapter == null) {
            return;
        }
        WebSocketContext context = adapter.context();
        WebSocketClose close = new WebSocketClose(1000, "done");
        runWithContext(context.service(), () -> handler.onClose(context, close));
    }

    /**
     * 处理连接错误。
     *
     * @param connection 原生连接
     * @param failure    失败
     */
    public void onError(WebSocketConnection connection, Throwable failure) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = removeAdapter(connection);
        if (adapter == null) {
            return;
        }
        WebSocketContext context = adapter.context();
        NexusException nexusError = failure instanceof NexusException nexusException
                ? nexusException
                : NexusException.build(NexusStatusCode.SYSTEM_ERROR, failure);
        runWithContext(context.service(), () -> handler.onError(context, nexusError));
        if (failure instanceof NexusException nexusFailure) {
            connection.sendText(errorPayload(nexusFailure)).subscribe().with(item -> {
            }, error -> {
            });
        }
    }

    private QuarkusWebSocketSessionAdapter<Object, Object> createAdapter(WebSocketConnection connection) {
        return new QuarkusWebSocketSessionAdapter<>(
                connection,
                transportSupport.buildContext(
                        "GET",
                        connection.handshakeRequest().path(),
                        QuarkusWebSocketSessionAdapter.handshakeHeaders(connection),
                        new CancellationSource()),
                codec,
                allowedOutputTypes);
    }

    private QuarkusWebSocketSessionAdapter<Object, Object> requireAdapter(WebSocketConnection connection) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = connection.userData().get(ADAPTER_KEY);
        if (adapter != null) {
            return adapter;
        }
        QuarkusWebSocketSessionAdapter<Object, Object> created = createAdapter(connection);
        connection.userData().put(ADAPTER_KEY, created);
        registry.register(created);
        return created;
    }

    private QuarkusWebSocketSessionAdapter<Object, Object> removeAdapter(WebSocketConnection connection) {
        QuarkusWebSocketSessionAdapter<Object, Object> adapter = connection.userData().remove(ADAPTER_KEY);
        if (adapter != null) {
            registry.unregister(adapter.connectionId());
        }
        return adapter;
    }

    private void runWithContext(
            com.innospots.nexus.service.contract.context.ServiceContext serviceContext,
            StageSupplier supplier) {
        ContextSnapshot snapshot = contexts.install(serviceContext);
        try {
            CompletionStage<?> stage = supplier.get();
            if (stage != null) {
                stage.toCompletableFuture().join();
            }
        } finally {
            contexts.restore(snapshot);
        }
    }

    private static WebSocketMessage<Object> castMessage(WebSocketMessage<?> message) {
        @SuppressWarnings("unchecked")
        WebSocketMessage<Object> cast = (WebSocketMessage<Object>) message;
        return cast;
    }

    private static String errorPayload(NexusException failure) {
        return Jsons.toJson(Map.of("code", failure.code(), "message", failure.getMessage()));
    }

    @FunctionalInterface
    private interface StageSupplier {
        CompletionStage<?> get();
    }
}
