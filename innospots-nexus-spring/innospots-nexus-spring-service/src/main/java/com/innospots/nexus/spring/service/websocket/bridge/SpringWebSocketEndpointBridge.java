package com.innospots.nexus.spring.service.websocket.bridge;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
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
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;

/**
 * 将 Spring Servlet WebSocket 回调转发到标准 {@link WebSocketHandler}。
 */
public class SpringWebSocketEndpointBridge extends TextWebSocketHandler {

    private static final String ADAPTER_ATTRIBUTE = SpringWebSocketSessionAdapter.class.getName();

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
     * @param contexts              线程上下文
     * @param inboundMessageHandler 入站治理派发（限流/舱壁/熔断/超时/权限）
     */
    public SpringWebSocketEndpointBridge(
            WebSocketHandler<Object, Object> handler,
            JsonWebSocketCodec codec,
            WebSocketRegistry registry,
            ServiceTransportSupport transportSupport,
            ThreadBoundServiceContext contexts,
            WebSocketInboundMessageHandler inboundMessageHandler) {
        this.handler = Checks.notNull(handler, "handler");
        this.codec = Checks.notNull(codec, "codec");
        this.registry = Checks.notNull(registry, "registry");
        this.transportSupport = Checks.notNull(transportSupport, "transportSupport");
        this.contexts = Checks.notNull(contexts, "contexts");
        this.inboundMessageHandler = Checks.notNull(inboundMessageHandler, "inboundMessageHandler");
        this.allowedOutputTypes = Set.copyOf(codec.registeredTypes());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        SpringWebSocketSessionAdapter<Object, Object> adapter = createAdapter(session);
        session.getAttributes().put(ADAPTER_ATTRIBUTE, adapter);
        registry.register(adapter);
        runWithContext(adapter.context().service(), () -> handler.onOpen(adapter));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SpringWebSocketSessionAdapter<Object, Object> adapter = requireAdapter(session);
        ServiceContext serviceContext = adapter.context().service();
        runWithContext(serviceContext, () -> {
            try {
                WebSocketMessage<?> decoded = codec.decode(
                        ByteBuffer.wrap(message.getPayload().getBytes(StandardCharsets.UTF_8)),
                        String.class);
                MessageDescriptor descriptor = codec.descriptor(decoded.type());
                return inboundMessageHandler.handle(handler, adapter, castMessage(decoded), descriptor);
            } catch (NexusException failure) {
                session.sendMessage(new TextMessage(errorPayload(failure)));
                return null;
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SpringWebSocketSessionAdapter<Object, Object> adapter = removeAdapter(session);
        if (adapter == null) {
            return;
        }
        WebSocketContext context = adapter.context();
        WebSocketClose close = new WebSocketClose(status.getCode(), status.getReason());
        runWithContext(context.service(), () -> handler.onClose(context, close));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        SpringWebSocketSessionAdapter<Object, Object> adapter = removeAdapter(session);
        if (adapter == null) {
            return;
        }
        WebSocketContext context = adapter.context();
        NexusException failure = exception instanceof NexusException nexusException
                ? nexusException
                : NexusException.build(NexusStatusCode.SYSTEM_ERROR, exception);
        runWithContext(context.service(), () -> handler.onError(context, failure));
        if (exception instanceof NexusException nexusFailure) {
            session.sendMessage(new TextMessage(errorPayload(nexusFailure)));
        }
    }

    private SpringWebSocketSessionAdapter<Object, Object> createAdapter(WebSocketSession session) {
        ServiceContext serviceContext = transportSupport.buildContext(
                "GET",
                path(session),
                SpringWebSocketSessionAdapter.handshakeHeaders(session.getHandshakeHeaders()),
                new CancellationSource());
        return new SpringWebSocketSessionAdapter<>(
                session,
                serviceContext,
                codec,
                allowedOutputTypes);
    }

    private void runWithContext(ServiceContext serviceContext, ThrowingCompletionStageSupplier supplier) throws Exception {
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

    @SuppressWarnings("unchecked")
    private SpringWebSocketSessionAdapter<Object, Object> requireAdapter(WebSocketSession session) {
        Object value = session.getAttributes().get(ADAPTER_ATTRIBUTE);
        if (value instanceof SpringWebSocketSessionAdapter<?, ?> adapter) {
            return (SpringWebSocketSessionAdapter<Object, Object>) adapter;
        }
        SpringWebSocketSessionAdapter<Object, Object> created = createAdapter(session);
        session.getAttributes().put(ADAPTER_ATTRIBUTE, created);
        registry.register(created);
        return created;
    }

    @SuppressWarnings("unchecked")
    private SpringWebSocketSessionAdapter<Object, Object> removeAdapter(WebSocketSession session) {
        Object value = session.getAttributes().remove(ADAPTER_ATTRIBUTE);
        if (value instanceof SpringWebSocketSessionAdapter<?, ?> adapter) {
            registry.unregister(adapter.connectionId());
            return (SpringWebSocketSessionAdapter<Object, Object>) adapter;
        }
        return null;
    }

    private static String path(WebSocketSession session) {
        if (session.getUri() == null) {
            return "/";
        }
        return session.getUri().getPath();
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
    private interface ThrowingCompletionStageSupplier {
        CompletionStage<?> get() throws Exception;
    }
}
