package com.innospots.nexus.service.websocket.governance;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.governance.config.GovernanceConfig;
import com.innospots.nexus.service.governance.config.RateLimitPolicy;
import com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider;
import com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor;
import com.innospots.nexus.service.contract.time.Ticker;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.websocket.handler.AbstractWebSocketHandler;
import com.innospots.nexus.service.websocket.message.FrameType;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;
import com.innospots.nexus.service.websocket.message.WebSocketMessage;
import com.innospots.nexus.service.websocket.session.WebSocketClose;
import com.innospots.nexus.service.websocket.session.WebSocketContext;
import com.innospots.nexus.service.websocket.session.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * WebSocket 入站消息经 {@link InvocationEngine} 限流的行为测试。
 */
class WebSocketGovernedMessageDispatcherTest {

    private static final String RATE_KEY = "ws.test.rate";

    @Test
    void rateLimitRejectsSecondMessageBeforeBusinessRuns() {
        GovernanceConfig config = new GovernanceConfig(
                Map.of(RATE_KEY, new RateLimitPolicy(1, 1.0D)),
                Map.of(),
                Map.of(),
                Map.of(),
                10_000,
                Duration.ofMinutes(15),
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofSeconds(30));
        RateLimitInterceptor rateLimit = new RateLimitInterceptor(
                new LocalTokenBucketProvider(config, Ticker.system()),
                config);
        ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
        InvocationEngine engine = new InvocationEngine(List.of(rateLimit), contexts);
        WebSocketGovernedMessageDispatcher dispatcher = new WebSocketGovernedMessageDispatcher(engine);
        MessageDescriptor descriptor = governedDescriptor();
        RecordingSession session = new RecordingSession(serviceContext(contexts));
        contexts.install(session.context().service());

        AtomicInteger businessRuns = new AtomicInteger();
        CompletionStage<Void> first = dispatcher.dispatch(session, descriptor, () -> {
            businessRuns.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        });
        first.toCompletableFuture().join();
        assertThat(businessRuns).hasValue(1);

        assertThatThrownBy(() -> dispatcher.dispatch(session, descriptor, () -> {
            businessRuns.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }).toCompletableFuture().join())
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.LIMIT_EXCEEDED.fullCode());
        assertThat(businessRuns).hasValue(1);
    }

    @Test
    void bypassesEngineWhenDescriptorHasNoGovernanceKeys() {
        ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
        InvocationEngine engine = new InvocationEngine(List.of(), contexts);
        WebSocketInboundMessageHandler handler = new WebSocketInboundMessageHandler(
                new WebSocketGovernedMessageDispatcher(engine),
                WebSocketRuntimeConfigForTest.defaults());
        MessageDescriptor plain = new MessageDescriptor(
                "plain",
                String.class,
                String.class,
                Set.of(),
                null,
                ExecutionMode.BLOCKING,
                FrameType.TEXT,
                null,
                null,
                null,
                null);
        AtomicInteger runs = new AtomicInteger();
        RecordingSession session = new RecordingSession(serviceContext(contexts));
        AbstractWebSocketHandler<Object, Object> countingHandler = new AbstractWebSocketHandler<>() {
            @Override
            public CompletionStage<Void> onMessage(WebSocketSession<Object, Object> s, WebSocketMessage<Object> m) {
                runs.incrementAndGet();
                return CompletableFuture.completedFuture(null);
            }
        };
        WebSocketMessage<Object> message = new WebSocketMessage<>(
                "m-1", "plain", null, 1L, Instant.parse("2026-09-15T10:00:00Z"), "x", Map.of());
        handler.handle(countingHandler, session, message, plain).toCompletableFuture().join();
        assertThat(runs).hasValue(1);
    }

    private static MessageDescriptor governedDescriptor() {
        return new MessageDescriptor(
                "chat.send",
                String.class,
                String.class,
                Set.of(),
                null,
                ExecutionMode.BLOCKING,
                FrameType.TEXT,
                RATE_KEY,
                null,
                null,
                null);
    }

    private static ServiceContext serviceContext(ThreadBoundServiceContext contexts) {
        ServiceContext context = new ServiceContext(
                "req-ws-gov",
                new RequestMetadata("GET", "/ws", "/ws", Map.of(), "127.0.0.1", null),
                ServicePrincipal.anonymous("adapter"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                com.innospots.nexus.service.contract.cancellation.CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        contexts.install(context);
        return context;
    }

    private static final class RecordingSession implements WebSocketSession<Object, Object> {

        private final WebSocketContext context;

        private RecordingSession(ServiceContext service) {
            this.context = new WebSocketContext("conn-1", "sess-1", service);
        }

        @Override
        public String connectionId() {
            return "conn-1";
        }

        @Override
        public String sessionId() {
            return "sess-1";
        }

        @Override
        public WebSocketContext context() {
            return context;
        }

        @Override
        public java.util.concurrent.Flow.Publisher<com.innospots.nexus.service.websocket.message.WebSocketMessage<Object>> inbound() {
            return subscriber -> subscriber.onComplete();
        }

        @Override
        public CompletionStage<Void> send(com.innospots.nexus.service.websocket.message.WebSocketMessage<Object> message) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<Void> close(WebSocketClose close) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean isOpen() {
            return true;
        }
    }

    private static final class WebSocketRuntimeConfigForTest {

        private WebSocketRuntimeConfigForTest() {
        }

        private static com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig defaults() {
            return com.innospots.nexus.service.websocket.config.WebSocketRuntimeConfig.defaults();
        }
    }
}
