package com.innospots.nexus.service.runtime.context;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 上下文传播嵌套恢复与跨线程安装测试。
 */
class ContextPropagationTest {

    private final ThreadBoundServiceContext contexts = new ThreadBoundServiceContext();
    private final ContextPropagation propagation = new ContextPropagation(contexts);

    @Test
    void nestedInstallRestoresOuterContext() {
        ServiceContext outer = context("outer");
        ServiceContext inner = context("inner");

        ContextSnapshot outerSnapshot = contexts.install(outer);
        ContextSnapshot innerSnapshot = contexts.install(inner);
        assertThat(contexts.requireCurrent().requestId()).isEqualTo("inner");

        contexts.restore(innerSnapshot);
        assertThat(contexts.requireCurrent().requestId()).isEqualTo("outer");

        contexts.restore(outerSnapshot);
        assertThat(contexts.current()).isEmpty();
    }

    @Test
    void wrappedRunnablePropagatesCapturedContext() {
        ServiceContext captured = context("worker");
        ContextSnapshot snapshot = contexts.install(captured);
        Runnable wrapped = propagation.wrap(() -> {
            assertThat(contexts.requireCurrent().requestId()).isEqualTo("worker");
        });
        contexts.restore(snapshot);

        assertThat(contexts.current()).isEmpty();
        wrapped.run();
        assertThat(contexts.current()).isEmpty();
    }

    @Test
    void wrappedCallableRestoresPreviousContextAfterExecution() throws Exception {
        ServiceContext outer = context("outer");
        ServiceContext captured = context("captured");
        contexts.install(outer);

        ContextSnapshot snapshot = contexts.install(captured);
        var wrapped = propagation.wrap(() -> contexts.requireCurrent().requestId());
        contexts.restore(snapshot);

        assertThat(contexts.requireCurrent().requestId()).isEqualTo("outer");
        assertThat(wrapped.call()).isEqualTo("captured");
        assertThat(contexts.requireCurrent().requestId()).isEqualTo("outer");
    }

    @Test
    void crossThreadSnapshotDoesNotClearOuterContext() throws Exception {
        ServiceContext outer = context("outer");
        ContextSnapshot snapshot = contexts.install(outer);
        AtomicReference<String> observed = new AtomicReference<>();

        CompletableFuture<Void> worker = CompletableFuture.runAsync(propagation.wrap(() ->
                observed.set(contexts.requireCurrent().requestId())));

        worker.join();
        contexts.restore(snapshot);

        assertThat(observed).hasValue("outer");
        assertThat(contexts.current()).isEmpty();
    }

    private static ServiceContext context(String requestId) {
        return new ServiceContext(
                requestId,
                new RequestMetadata("GET", "/demo", "/demo", Map.of(), "127.0.0.1", null),
                ServicePrincipal.anonymous("local"),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
    }
}
