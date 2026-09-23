package com.innospots.nexus.spring.service.websocket.bridge;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import org.springframework.web.reactive.socket.WebSocketMessage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.runtime.context.ContextSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.cancellation.CancellationSource;
import com.innospots.nexus.service.websocket.governance.WebSocketInboundMessageHandler;
import com.innospots.nexus.service.websocket.handler.WebSocketHandler;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;
import com.innospots.nexus.service.websocket.registry.WebSocketRegistry;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;

import reactor.core.publisher.Mono;

/**
 * 将 Spring WebFlux WebSocket 回调转发到标准 {@link WebSocketHandler}。
 */
public class SpringReactiveWebSocketEndpointBridge implements org.springframework.web.reactive.socket.WebSocketHandler {

    private static final String ADAPTER_ATTRIBUTE = SpringReactiveWebSocketSessionAdapter.class.getName();

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
     * @param inboundMessageHandler 入站治理派发
     */
    public SpringReactiveWebSocketEndpointBridge(
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
    public Mono<Void> handle(org.springframework.web.reactive.socket.WebSocketSession session) {
        SpringReactiveWebSocketSessionAdapter<Object, Object> adapter = createAdapter(session);
        session.getAttributes().put(ADAPTER_ATTRIBUTE, adapter);
        registry.register(adapter);
        ServiceContext serviceContext = adapter.context().service();
        return runWithContext(serviceContext, () -> handler.onOpen(adapter))
                .thenMany(session.receive())
                .concatMap(message -> handleMessage(session, adapter, message))
                .then(runWithContext(serviceContext, () -> {
                    registry.unregister(adapter.connectionId());
                    WebSocketContext context = adapter.context();
                    return handler.onClose(context, new WebSocketClose(1000, "done"));
                }))
                .then();
    }

    private Mono<Void> handleMessage(
            org.springframework.web.reactive.socket.WebSocketSession session,
            SpringReactiveWebSocketSessionAdapter<Object, Object> adapter,
            WebSocketMessage message) {
        return runWithContext(adapter.context().service(), () -> {
            try {
                com.innospots.nexus.service.websocket.message.WebSocketMessage<?> decoded = codec.decode(
                        ByteBuffer.wrap(message.getPayloadAsText().getBytes(StandardCharsets.UTF_8)),
                        String.class);
                MessageDescriptor descriptor = codec.descriptor(decoded.type());
                return inboundMessageHandler.handle(handler, adapter, castMessage(decoded), descriptor);
            } catch (NexusException failure) {
                return session.send(Mono.just(session.textMessage(errorPayload(failure)))).toFuture();
            }
        });
    }

    private SpringReactiveWebSocketSessionAdapter<Object, Object> createAdapter(
            org.springframework.web.reactive.socket.WebSocketSession session) {
        Map<String, java.util.List<String>> headers = new java.util.LinkedHashMap<>();
        session.getHandshakeInfo().getHeaders().forEach((name, values) -> {
            headers.put(name.toLowerCase(Locale.ROOT), java.util.List.copyOf(values));
        });
        ServiceContext serviceContext = transportSupport.buildContext(
                "GET",
                session.getHandshakeInfo().getUri().getPath(),
                headers,
                new CancellationSource());
        return new SpringReactiveWebSocketSessionAdapter<>(
                session,
                serviceContext,
                codec,
                allowedOutputTypes);
    }

    private Mono<Void> runWithContext(ServiceContext serviceContext, StageSupplier supplier) {
        ContextSnapshot snapshot = contexts.install(serviceContext);
        CompletionStage<?> stage = supplier.get();
        if (stage == null) {
            contexts.restore(snapshot);
            return Mono.empty();
        }
        return Mono.fromFuture(stage.toCompletableFuture()).then().doFinally(signal -> contexts.restore(snapshot));
    }

    private static com.innospots.nexus.service.websocket.message.WebSocketMessage<Object> castMessage(
            com.innospots.nexus.service.websocket.message.WebSocketMessage<?> message) {
        @SuppressWarnings("unchecked")
        com.innospots.nexus.service.websocket.message.WebSocketMessage<Object> cast =
                (com.innospots.nexus.service.websocket.message.WebSocketMessage<Object>) message;
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
