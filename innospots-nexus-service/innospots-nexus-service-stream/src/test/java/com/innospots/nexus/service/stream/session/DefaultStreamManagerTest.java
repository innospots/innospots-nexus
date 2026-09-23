package com.innospots.nexus.service.stream.session;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.contract.channel.OverflowPolicy;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.event.StreamEventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 流管理器容量、订阅超时、鉴权与关闭测试。
 */
class DefaultStreamManagerTest {

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
    private final java.util.concurrent.ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    @AfterEach
    void shutdownScheduler() {
        scheduler.shutdownNow();
    }

    @Test
    void enforcesMaxSessions() {
        DefaultStreamManager manager = manager(base -> new StreamConfig(
                1,
                base.bufferSize(),
                base.bufferBytes(),
                base.globalBufferBytes(),
                base.maxEventBytes(),
                base.overflow(),
                base.subscribeTimeout(),
                base.ttl(),
                base.idleTimeout(),
                base.heartbeat(),
                base.terminalWriteTimeout()));
        installUser("owner");
        manager.open(String.class);

        assertThatThrownBy(() -> manager.open(String.class))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(com.innospots.nexus.service.contract.status.ServiceStatusCode.CAPACITY_EXHAUSTED.fullCode());
    }

    @Test
    void subscribeTimeoutCancelsUnsubscribedSession() throws Exception {
        DefaultStreamManager manager = manager(config -> new StreamConfig(
                4,
                8,
                4096,
                8192,
                1024,
                OverflowPolicy.REJECT,
                Duration.ofMillis(50),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1),
                Duration.ofSeconds(15),
                Duration.ofSeconds(2)));
        installUser("owner");
        StreamSink<String> sink = manager.open(String.class);

        TimeUnit.MILLISECONDS.sleep(100);

        assertThat(sink.state()).isEqualTo(StreamState.CANCELLED);
    }

    @Test
    void heartbeatDoesNotRefreshBusinessActivity() {
        DefaultStreamManager manager = manager(config -> config);
        installUser("owner");
        DefaultStreamSession<String> session = (DefaultStreamSession<String>) manager.open(String.class);
        java.time.Instant createdActivity = session.lastActivityAt();

        session.emit(StreamEventType.HEARTBEAT.wireName(), "ping").toCompletableFuture().join();

        assertThat(session.lastActivityAt()).isEqualTo(createdActivity);
        session.emit("business").toCompletableFuture().join();
        assertThat(session.lastActivityAt()).isAfter(createdActivity);
    }

    @Test
    void emitBySessionIdChecksOwnerAndType() {
        DefaultStreamManager manager = manager(config -> config);
        installUser("owner");
        StreamSink<String> sink = manager.open(String.class);
        String sessionId = sink.sessionId();

        manager.emit(sessionId, "message", "ok").toCompletableFuture().join();

        installUser("other");
        assertThatThrownBy(() -> manager.emit(sessionId, "message", "nope"))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.NO_PERMISSION.fullCode());

        installUser("owner");
        assertThatThrownBy(() -> manager.emit(sessionId, "message", 1).toCompletableFuture().join())
                .hasCauseInstanceOf(NexusException.class)
                .cause()
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.INVALID_PARAMETER.fullCode());
    }

    @Test
    void closeCancelsActiveSessions() {
        DefaultStreamManager manager = manager(config -> config);
        installUser("owner");
        StreamSink<String> first = manager.open(String.class);
        StreamSink<String> second = manager.open(String.class);

        manager.close().toCompletableFuture().join();

        assertThat(first.state()).isEqualTo(StreamState.CANCELLED);
        assertThat(second.state()).isEqualTo(StreamState.CANCELLED);
        assertThat(manager.activeSessions()).isZero();
    }

    @Test
    void completeRemovesSessionFromRegistry() {
        DefaultStreamManager manager = manager(config -> config);
        installUser("owner");
        StreamSink<String> sink = manager.open(String.class);
        String sessionId = sink.sessionId();

        sink.complete().toCompletableFuture().join();

        assertThat(sink.state()).isEqualTo(StreamState.COMPLETED);
        assertThat(manager.activeSessions()).isZero();
        assertThat(manager.find(sessionId)).isEmpty();
    }

    private DefaultStreamManager manager(java.util.function.Function<StreamConfig, StreamConfig> adjust) {
        StreamConfig base = new StreamConfig(
                4,
                8,
                4096,
                8192,
                1024,
                OverflowPolicy.REJECT,
                Duration.ofSeconds(30),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1),
                Duration.ofSeconds(15),
                Duration.ofSeconds(2));
        return DefaultStreamManager.builder()
                .config(adjust.apply(base))
                .contexts(contexts)
                .scheduler(scheduler)
                .build();
    }

    private void installUser(String userId) {
        ServiceContext context = new ServiceContext(
                "req-" + userId,
                new RequestMetadata("GET", "/stream", "/stream", Map.of(), "127.0.0.1", null),
                new ServicePrincipal(userId, PrincipalType.USER, "local", null, null, null),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        contexts.install(context);
    }
}
