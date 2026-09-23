package com.innospots.nexus.service.runtime.idempotency;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.InvocationOutcome;
import com.innospots.nexus.service.contract.invocation.OutcomeType;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 幂等协调拦截器。
 */
public final class IdempotencyCoordinator implements ServiceInterceptor {

    private static final String IDEMPOTENCY_HEADER = "idempotency-key";

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final int DEFAULT_MAX_ENTRIES = 10_000;

    private final boolean idempotencyEnabled;
    private final InMemoryIdempotencyStore store;

    /**
     * 创建协调器。
     *
     * @param idempotencyEnabled 是否启用幂等
     */
    public IdempotencyCoordinator(boolean idempotencyEnabled) {
        this(idempotencyEnabled, new InMemoryIdempotencyStore(DEFAULT_MAX_ENTRIES, DEFAULT_TTL));
    }

    /**
     * 创建协调器。
     *
     * @param idempotencyEnabled 是否启用幂等
     * @param store              幂等存储
     */
    IdempotencyCoordinator(boolean idempotencyEnabled, InMemoryIdempotencyStore store) {
        this.idempotencyEnabled = idempotencyEnabled;
        this.store = Checks.notNull(store, "store");
    }

    @Override
    public String id() {
        return InterceptorIds.IDEMPOTENCY;
    }

    @Override
    public int order() {
        return InterceptorOrders.IDEMPOTENCY;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        if (!idempotencyEnabled) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        String rawKey = headerValue(invocation, IDEMPOTENCY_HEADER);
        if (rawKey == null) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        IdempotencyKey key = new IdempotencyKey(
                principalRealm(invocation),
                scopeKey(invocation.service().scope()),
                invocation.service().security().id(),
                invocation.operationId(),
                rawKey);
        String fingerprint = fingerprint(invocation);
        InMemoryIdempotencyStore.AdmissionDecision decision = store.admit(key, fingerprint);
        if (decision == InMemoryIdempotencyStore.AdmissionDecision.CONFLICT
                || decision == InMemoryIdempotencyStore.AdmissionDecision.IN_FLIGHT) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.IDEMPOTENCY_CONFLICT));
        }
        return CompletableFuture.completedFuture(outcome -> {
            store.complete(key, mapState(outcome));
            return CompletableFuture.completedFuture(null);
        });
    }

    private static IdempotencyState mapState(InvocationOutcome outcome) {
        return switch (outcome.type()) {
            case SUCCEEDED -> IdempotencyState.SUCCEEDED;
            case TIMED_OUT, CANCELLED -> IdempotencyState.UNKNOWN;
            case FAILED, REJECTED -> IdempotencyState.FAILED;
        };
    }

    private static String fingerprint(InvocationContext invocation) {
        String material = invocation.operationId()
                + "|"
                + invocation.resource().type()
                + "|"
                + invocation.resource().id();
        return CryptoUtils.sha256Hex(material);
    }

    private static String headerValue(InvocationContext invocation, String headerName) {
        List<String> values = invocation.service().request().headers().get(headerName.toLowerCase());
        if (values == null || values.isEmpty()) {
            return null;
        }
        String value = values.getFirst();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String principalRealm(InvocationContext invocation) {
        String realm = invocation.service().security().realm();
        if (realm == null || realm.isBlank()) {
            return "default";
        }
        return realm;
    }

    private static String scopeKey(ServiceScope scope) {
        return String.join(":",
                nullToEmpty(scope.tenantId()),
                nullToEmpty(scope.workspaceId()),
                nullToEmpty(scope.projectId()));
    }

    private static String nullToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
