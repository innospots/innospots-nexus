package com.innospots.nexus.service.observability.trace;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.contract.context.ContextAttributes;
import com.innospots.nexus.service.contract.context.RequestMetadata;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.time.Deadline;
import com.innospots.nexus.service.contract.trace.TraceSnapshot;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 无 SDK 追踪回退测试。
 */
class NoOpTraceProviderTest {

    @Test
    void returnsEmptySnapshotWithoutFakeIds() {
        NoOpTraceProvider provider = new NoOpTraceProvider();
        TraceSnapshot snapshot = provider.start(sampleInvocation(), TraceSnapshot.empty()).snapshot();

        assertThat(snapshot.traceId()).isEmpty();
        assertThat(snapshot.spanId()).isEmpty();
        assertThat(snapshot.sampled()).isFalse();
    }

    private static InvocationContext sampleInvocation() {
        ServiceContext service = new ServiceContext(
                "req-1",
                new RequestMetadata("GET", "/demo", "/demo", java.util.Map.of(), "127.0.0.1", null),
                new ServicePrincipal("user-1", PrincipalType.USER, "local", null, null, null),
                ServiceScope.platform(),
                TraceSnapshot.empty(),
                com.innospots.nexus.service.contract.cancellation.CancellationToken.none(),
                Deadline.unlimited(),
                ContextAttributes.empty());
        return new InvocationContext(
                "inv-1",
                "demo.operation",
                service,
                new OperationPolicy(null, null, null, null, null, null, null, false, null, null, null, null, null),
                new ResourceRef("demo", "demo-1", ServiceScope.platform()));
    }
}
